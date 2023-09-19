package example.combiner;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static example.Log.logErr;
import static example.Log.logInfo;

public class StreamConsumerCluster {

    private final Map<String, StreamConsumer> consumers;
    private final List<Thread> consumerTasks = new ArrayList<>();
    private final StreamMerger4 streamMerger;

    /**
     * stream combiner represents a cluster of stream consumers and a shared buffer to merge individual streams
     */
    public StreamConsumerCluster(Map<String, StreamConsumer> consumers, StreamMerger4 streamMerger) {
        this.consumers = consumers;
        this.streamMerger = streamMerger;
    }

    public static Map<String, StreamConsumer> consumersFromConfig(Config config, XmlMapper xmlMapper, StreamMerger4 streamMerger) {

        Map<String, StreamConsumer> consumers = new HashMap<>();
        for (int i = 0; i < config.getProducers().size(); i++) {
            ProducerTarget target = config.getProducers().get(i);
            String name = String.format("virtual-%d-consumer-%s", i + 1, target);
            StreamConsumer consumer = new StreamConsumer(name, target, config.getSocketReceiveTimeout(), xmlMapper, streamMerger);
            consumers.put(name, consumer);
        }
        return consumers;
    }

    public void shutdown() {
        for (StreamConsumer consumer : consumers.values()) {
            try {
                consumer.shutdown();
            } catch (IOException e) {
                logErr(e, "[consumer-cluster] failed to shutdown consumer '%s'", consumer);
            }
        }
        streamMerger.flushAll();
    }

    /**
     * starts 1..N stream consumers with virtual threads and blocks to wait all
     */
    public void start() {

        logInfo("[consumer-cluster] scheduling %d stream consumer tasks", consumers.size());
        for (String name : consumers.keySet()) {
            logInfo("[consumer-cluster] scheduling stream consumer task '%s'", name);
            StreamConsumer consumer = consumers.get(name);
            Thread task = Thread.ofVirtual().name(name).start(consumer::start);
            consumerTasks.add(task);
        }

        for (Thread t : consumerTasks) {
            try {
                t.join();
            } catch (InterruptedException e) {
                logErr(e, "[consumer-cluster] ignoring interrupted thread");
            }
        }
    }

}
