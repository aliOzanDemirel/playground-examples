package example;

import example.producer.Config;
import example.producer.StreamProducer;
import example.producer.StreamProducerCluster;

import java.util.Map;

import static example.Log.logErr;
import static example.Log.logInfo;

public class Main {

    public static void main(String[] args) {

        Config conf = null;
        try {
            conf = Config.load();
            conf.validate();
        } catch (Exception e) {
            logErr(e, "config error");
            System.exit(1);
        }

        Map<String, StreamProducer> producers = StreamProducerCluster.producersFromConfig(conf);
        StreamProducerCluster producerCluster = new StreamProducerCluster(producers);
        handleShutdown(producerCluster);
        producerCluster.start();
    }

    // handle SIGTERM and SIGINT
    private static void handleShutdown(StreamProducerCluster producerCluster) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logInfo("[shutdown-hook] shutting down producers");
            producerCluster.shutdown();
        }));
    }
}
