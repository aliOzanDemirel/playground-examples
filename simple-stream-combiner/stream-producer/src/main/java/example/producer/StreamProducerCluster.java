package example.producer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static example.Log.logErr;
import static example.Log.logInfo;

public class StreamProducerCluster {

    private final Map<String, StreamProducer> producers;
    private final List<Thread> producerTasks = new ArrayList<>();

    /**
     * configures potentially many producer tasks that either generate random data or read data from file
     * first producers will be configured to read xml data from file if there is any file path configured
     * remaining producers (if any) will be configured to read random xml data
     */
    public StreamProducerCluster(Map<String, StreamProducer> producers) {
        this.producers = producers;
    }

    public static Map<String, StreamProducer> producersFromConfig(Config config) {

        Map<String, StreamProducer> producers = new HashMap<>();
        int countOfHowManyProducersWithMockFile = config.getXmlDataFilePaths().size();
        for (int i = 0; i < countOfHowManyProducersWithMockFile; i++) {
            String path = config.getXmlDataFilePaths().get(i);
            int port = config.getSocketPorts().get(i);
            String name = String.format("virtual-%d-source-file-%d", i + 1, port);
            StreamProducer producer = new StreamProducer(name, port, new FileDataProvider(path));
            producers.put(name, producer);
        }

        for (int i = countOfHowManyProducersWithMockFile; i < config.getSocketPorts().size(); i++) {
            int port = config.getSocketPorts().get(i);
            String name = String.format("virtual-%d-source-random-%d", i + 1, port);
            StreamProducer producer = new StreamProducer(name, port, new RandomDataProvider());
            producers.put(name, producer);
        }
        return producers;
    }

    public void shutdown() {
        producers.values().forEach(producer -> {
            try {
                producer.shutdown();
            } catch (IOException e) {
                logErr(e, "[producer-cluster] failed to shutdown producer '%s'", producer);
            }
        });
    }

    /**
     * starts 1..N stream producers with virtual threads and blocks to wait all
     */
    public void start() {

        logInfo("[producer-cluster] scheduling %d stream producer tasks", producers.size());
        for (String name : producers.keySet()) {
            logInfo("[producer-cluster] scheduling stream producer task '%s'", name);
            StreamProducer producer = producers.get(name);
            Thread task = Thread.ofVirtual().name(name).start(producer::start);
            producerTasks.add(task);
        }

        for (Thread t : producerTasks) {
            try {
                t.join();
            } catch (InterruptedException e) {
                logErr(e, "[producer-cluster] ignoring interrupted thread");
            }
        }
    }
}
