package example.combiner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static example.Log.logErr;
import static example.Log.logInfo;

public class StreamConsumer {

    private static final String SIGNAL_END = "finish-xml-data-stream";

    private Socket clientSocket;
    private final Lock socketMutex = new ReentrantLock();

    private final String name;
    private final ProducerTarget producerTarget;
    private final Duration socketReceiveTimeout;
    private final XmlMapper xmlMapper;
    private final StreamMerger4 streamMerger;

    /**
     * NOTE: alternative implementation can be done with consumers having an internal buffer 'queue' with some configurable capacity
     * simple backpressure can be added to drop xml data if buffer fills, and also signal this issue back to producer
     * a combiner thread can then poll these registered queues and sort the records by merging sorted streams
     */
    public StreamConsumer(String name, ProducerTarget producerTarget, Duration socketReceiveTimeout, XmlMapper xmlMapper, StreamMerger4 streamMerger) {
        this.name = name;
        this.producerTarget = producerTarget;
        this.socketReceiveTimeout = socketReceiveTimeout;
        this.xmlMapper = xmlMapper;
        this.streamMerger = streamMerger;
        this.streamMerger.registerStream(name);
    }

    /**
     * disconnect clients explicitly when java process is terminated
     */
    public void shutdown() throws IOException {
        try {
            socketMutex.lock();
            if (clientSocket == null) {
                return;
            }
            clientSocket.shutdownInput();
            clientSocket.shutdownOutput();
            clientSocket.close();
            clientSocket = null;
        } finally {
            socketMutex.unlock();
        }
    }

    /**
     * consumer is the receiving end of producer stream, connects to single producer and pushes xml data to configured buffer
     * this method starts the reading loop and caches a reference to the client socket
     * if producer does not send anything in some configured time interval, then consumer drops the connection
     */
    public void start() {
        try (
                Socket socket = new Socket(producerTarget.getHost(), producerTarget.getPort());
                BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            logInfo("[%s] connected to producer -> %s", name, socket);
            try {
                socketMutex.lock();
                this.clientSocket = socket;
            } finally {
                socketMutex.unlock();
            }

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
                    XmlData data = xmlMapper.readValue(line, XmlData.class);
                    logInfo("[%s] received and deserialized new line -> %s", name, data);
                    streamMerger.add(name, data);

                } catch (JsonProcessingException e) {
                    logErr(e, "[%s] could not deserialize, dropping data -> %s", name, line);
                } catch (StreamMerger4.BufferOverCapacityException e) {
                    // NOTE: this can somehow be signalled to producer
                    logErr(e, "[%s] could not buffer the record, dropping data -> %s", name, line);
                }
            }
        } catch (SocketTimeoutException e) {
            logErr(e, "[%s] timeout waiting for xml message, producer might have hanged", name);
        } catch (Exception e) {
            logErr(e, "[%s] disconnected -> ", name);
        } finally {
            logInfo("[%s] connection is closed", name);

            // if a consumer is disconnected, its stream will be cleaned from merger
            // so that merger will continue to work as is, with just one less stream
            streamMerger.deregisterStream(name);
        }
    }

    @Override
    public String toString() {
        return "StreamConsumer [" + name + "] -> producerTarget=" + producerTarget;
    }
}
