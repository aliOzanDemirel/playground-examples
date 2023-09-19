package example.producer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static example.Log.logErr;
import static example.Log.logInfo;

public class StreamProducer {

    private static final String SIGNAL_END = "finish-xml-data-stream";

    private ServerSocket serverSocket;
    private final Lock socketMutex = new ReentrantLock();

    private final String name;
    private final int socketPort;
    private final XmlDataProvider xmlDataProvider;

    public StreamProducer(String name, int socketPort, XmlDataProvider xmlDataProvider) {
        this.name = name;
        this.socketPort = socketPort;
        this.xmlDataProvider = xmlDataProvider;
    }

    /**
     * closes server socket
     */
    public void shutdown() throws IOException {
        try {
            socketMutex.lock();
            if (this.serverSocket == null) {
                return;
            }
            this.serverSocket.close();
        } finally {
            socketMutex.unlock();
        }
    }

    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(socketPort)) {
            logInfo("[%s] listening on server socket -> %s", name, serverSocket);
            try {
                socketMutex.lock();
                this.serverSocket = serverSocket;
            } finally {
                socketMutex.unlock();
            }

            // wait for client connections in infinite loop
            // but this is all in single thread so single client will always block single socket
            while (true) {
                try {
                    acceptClient(serverSocket);
                } catch (Exception e) {
                    logErr(e, "[%s] client is disconnected, will wait for new one", name);
                }
            }
        } catch (Exception e) {
            logErr(e, "[%s] could not listen on server socket", name);
        }
    }

    void acceptClient(ServerSocket serverSocket) throws Exception {

        try (
                Socket clientSocket = serverSocket.accept();
                PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
            logInfo("[%s] client is connected -> %s", name, clientSocket);
            clientSocket.setKeepAlive(true);

            try (Stream<String> streamOfXmlData = xmlDataProvider.streamXmlData()) {

                // potentially infinite stream of xml data messages are logged and written to client
                streamOfXmlData.forEach(xmlString -> {

                    // break out of stream if client is determined to be disconnected
                    // so producer will wait for a different connection if existing one was broken
                    if (isClientEndClosed(clientSocket)) {
                        throw new RuntimeException("client dropped connection");
                    }

                    logInfo("[%s] emitting -> %s", name, xmlString);
                    toClient.println(xmlString);
                    toClient.flush();
                });
            }

            // done with the xml messages, signal to client that stream will be closed
            toClient.println(SIGNAL_END);

        } finally {
            logInfo("[%s] connection is closed", name);
        }
    }

    private boolean isClientEndClosed(Socket clientSocket) {
        try {
            // block 2 milliseconds to determine if stream is closed at the client end
            clientSocket.setSoTimeout(2);
            int bytes = clientSocket.getInputStream().read();
            return bytes == -1;
        } catch (SocketTimeoutException e) {
            // treat this socket timeout as 'client end of socket is still open'
            return false;
        } catch (IOException e) {
            logInfo("[%s] client end of socket is disconnected -> %s", name, e.getMessage());
            return true;
        }
    }

    @Override
    public String toString() {
        return "StreamProducer [" + name + "]";
    }
}
