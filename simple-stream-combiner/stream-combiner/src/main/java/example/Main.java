package example;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import example.combiner.Config;
import example.combiner.JsonOutput;
import example.combiner.StreamCombiner;
import example.combiner.StreamMerger3;

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
        StreamMerger3 streamMerger = new StreamMerger3(new JsonOutput.StdoutWriter(), conf.getProducers().size());
        StreamCombiner combiner = new StreamCombiner(conf, xmlMapper, streamMerger);
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