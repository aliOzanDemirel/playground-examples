package example.combiner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Duration;

import static example.Log.logErr;
import static example.Log.logInfo;

public class StreamConsumer {

    private static final String SIGNAL_END = "finish-xml-data-stream";

    private final String name;
    private final ProducerTarget producerTarget;
    private final Duration socketReceiveTimeout;
    private final XmlMapper xmlMapper;
    private final StreamMerger3 streamMerger;
    private Socket clientSocket;

    public StreamConsumer(String name, ProducerTarget producerTarget, Duration socketReceiveTimeout, XmlMapper xmlMapper, StreamMerger3 streamMerger) {
        this.name = name;
        this.producerTarget = producerTarget;
        this.socketReceiveTimeout = socketReceiveTimeout;
        this.xmlMapper = xmlMapper;
        this.streamMerger = streamMerger;
    }

    // disconnect clients explicitly when java process is terminated
    // ignoring concurrent access to clientSocket reference here
    public void shutdown() throws IOException {
        if (clientSocket == null) {
            return;
        }
        clientSocket.shutdownInput();
        clientSocket.shutdownOutput();
        clientSocket.close();
        clientSocket = null;
    }

    /**
     * consumer is the receiving and of producer stream, connects to single producer and pushes xml data to configured buffer
     * this method starts the reading loop and caches a reference to the client socket
     */
    public void start() {

        // TODO: these consumers could have an internal buffer 'queue' with some configurable limit on its size
        //  and if this buffer fills, producers can be signalled for some simple backpressure while the data is dropped
        //  a different implementation of merger can then poll these registered queues in single thread

        // TODO: whenever a consumer is disconnected, merger should be cleaned and should continue to work with one less consumer
        try (
                Socket socket = new Socket(producerTarget.getHost(), producerTarget.getPort());
                BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            this.clientSocket = socket;
            logInfo("[%s] connected to producer -> %s", name, socket);

            // close the connection and drop the producer if it did not send anything for long time
            socket.setSoTimeout((int) socketReceiveTimeout.toMillis());

            // read until \n, \r, \r\n, EOF, which corresponds to a single xml data
            String line;
            while ((line = fromServer.readLine()) != null) {

                // wait no more, there won't be any more message coming
                if (SIGNAL_END.equals(line)) {
                    logInfo("[%s] received signal to not expect more messages, closing connection", name);
                    break;
                }

                try {
                    // TODO: can have a simple implementation of InputStreamReader to explicitly parse the expected xml format
                    XmlData data = xmlMapper.readValue(line, XmlData.class);
                    logInfo("[%s] received and deserialized new line -> %s", name, data);

                    streamMerger.add(name, data);

                } catch (JsonProcessingException e) {
                    logErr(e, "[%s] could not deserialize, dropping data -> %s", name, line);
                }
            }

        } catch (SocketTimeoutException e) {
            logErr(e, "[%s] timeout waiting for xml message, producer might have hanged", name);
        } catch (Exception e) {
            logErr(e, "[%s] disconnected -> ", name);
        } finally {
            logInfo("[%s] connection is closed", name);
        }
    }

    @Override
    public String toString() {
        return "StreamConsumer [" + name + "] -> producerTarget=" + producerTarget;
    }
}
