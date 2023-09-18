package example;

import example.producer.Config;
import example.producer.StreamProducerCluster;

import static example.Log.logErr;

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

        StreamProducerCluster producers = new StreamProducerCluster(conf);
        producers.start();
    }
}
