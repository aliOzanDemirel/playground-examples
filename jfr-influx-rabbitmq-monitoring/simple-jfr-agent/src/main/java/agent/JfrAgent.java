package agent;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;

import java.lang.instrument.Instrumentation;
import java.time.Duration;
import java.util.logging.Logger;

public class JfrAgent implements Runnable {

    private static final Logger javaLog = Logger.getLogger("JfrAgent");

    public static void premain(String agentArgs, Instrumentation inst) {
        try {
            javaLog.info("Attaching JFR Monitor");
            new Thread(new JfrAgent()).start();
        } catch (Throwable t) {
            javaLog.severe("Could not attach JFR Monitor " + t.getMessage());
        }
    }

    public void run() {

        javaLog.info("Started JFR Monitor");

        var reviewAddedEventConsumer = new ReviewAddedEventConsumer();
        try (var recording = new RecordingStream()) {

            Duration duration = Duration.ofSeconds(10);

            recording.enable("jdk.GCConfiguration").withPeriod(duration);
            recording.onEvent("jdk.GCConfiguration", JfrAgent::onGcConfiguration);

            recording.enable("jdk.GCHeapSummary").withPeriod(duration);
            recording.onEvent("jdk.GCHeapSummary", JfrAgent::onGcHeapSummary);

            recording.enable("jdk.JavaThreadStatistics").withPeriod(duration);
            recording.onEvent("jdk.JavaThreadStatistics", JfrAgent::onJavaThreadStatistics);

            recording.enable("jdk.CPULoad").withPeriod(duration);
            recording.onEvent("jdk.CPULoad", JfrAgent::onCpuLoad);

            // custom event thrown by application code
            recording.enable("service.ReviewAdded").withoutStackTrace().withoutThreshold();
            recording.onEvent("service.ReviewAdded", reviewAddedEventConsumer);

            recording.start();
            Runtime.getRuntime().addShutdownHook(new Thread(recording::close));
        }
    }

    private static void onGcConfiguration(RecordedEvent event) {
        javaLog.info("onGcConfiguration -> oldCollector: " + event.getString("oldCollector")
                + " youngCollector: " + event.getString("youngCollector"));
    }

    private static void onGcHeapSummary(RecordedEvent event) {
        javaLog.info("onGcHeapSummary -> heapUsed: " + event.getLong("heapUsed")
                + " heapSpace.committedSize: " + event.getLong("heapSpace.committedSize"));
    }

    private static void onCpuLoad(RecordedEvent event) {
        javaLog.info("onCpuLoad -> jvmSystem: " + event.getDouble("jvmSystem")
                + " jvmUser: " + event.getDouble("jvmUser")
                + " machineTotal: " + event.getDouble("machineTotal"));
    }

    private static void onJavaThreadStatistics(RecordedEvent event) {
        javaLog.info("onJavaThreadStatistics -> activeCount: " + event.getValue("activeCount"));
    }

}
