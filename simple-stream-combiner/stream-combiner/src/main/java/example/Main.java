package example;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import example.combiner.*;

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

        XmlMapper xmlMapper = XmlMapper.builder().build();
        StreamMerger4 streamMerger = new StreamMerger4(conf.getCombinerMergeBufferCapacity(), new JsonOutput.StdoutWriter());

        Map<String, StreamConsumer> consumers = StreamCombiner.consumersFromConfig(conf, xmlMapper, streamMerger);
        StreamCombiner combiner = new StreamCombiner(consumers, streamMerger);
        handleShutdown(combiner);
        combiner.start();
    }

    // handle SIGTERM and SIGINT
    private static void handleShutdown(StreamCombiner combiner) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logInfo("[shutdown-hook] shutting down stream combiner");
            combiner.shutdown();
        }));
    }
}