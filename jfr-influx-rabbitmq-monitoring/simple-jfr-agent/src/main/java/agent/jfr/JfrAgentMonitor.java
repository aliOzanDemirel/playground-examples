package agent.jfr;

import agent.bond.BondIssuedHandler;
import agent.clothing.ReviewAddedHandler;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class JfrAgentMonitor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(JfrAgentMonitor.class);

    // interval for consuming jdk internal jfr events
    private static final Duration JDK_EVENT_PROCESSING_PERIOD = Duration.ofSeconds(20);

    public void run() {

        delaySomeTime();

        try (var recording = new RecordingStream()) {

            // custom events thrown by application code
            if (AgentConfig.isAttachedToClothingService()) {

                var clothingReviewEventName = "clothing.ClothingReviewAdded";
                recording.enable(clothingReviewEventName).withoutStackTrace().withoutThreshold();
                recording.onEvent(clothingReviewEventName, new ReviewAddedHandler());
                log.info("Enabled JFR event: {}", clothingReviewEventName);

            } else if (AgentConfig.isAttachedToBondIssuer()) {

                var bondIssuedEventName = "bond.BondIssued";
                recording.enable(bondIssuedEventName).withoutStackTrace().withoutThreshold();
                recording.onEvent(bondIssuedEventName, new BondIssuedHandler());
                log.info("Enabled JFR event: {}", bondIssuedEventName);
            }

            enableJdkEvents(recording);
            recording.start();
            log.info("Started JFR Monitor...");

            Runtime.getRuntime().addShutdownHook(new Thread(recording::close));
        }
    }

    // sort of wait for application startup
    private void delaySomeTime() {

        var duration = Duration.ofSeconds(10);
        log.info("Waiting {} seconds to attach JFR Monitor...", duration.toSeconds());
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            log.warn("JFR starter is interrupted!");
            Thread.currentThread().interrupt();
        }
    }

    private void enableJdkEvents(RecordingStream recording) {

        recording.enable("jdk.GCConfiguration").withPeriod(JDK_EVENT_PROCESSING_PERIOD);
        recording.onEvent("jdk.GCConfiguration", JfrAgentMonitor::onGcConfiguration);

        recording.enable("jdk.GCHeapSummary").withPeriod(JDK_EVENT_PROCESSING_PERIOD);
        recording.onEvent("jdk.GCHeapSummary", JfrAgentMonitor::onGcHeapSummary);

        recording.enable("jdk.JavaThreadStatistics").withPeriod(JDK_EVENT_PROCESSING_PERIOD);
        recording.onEvent("jdk.JavaThreadStatistics", JfrAgentMonitor::onJavaThreadStatistics);

        recording.enable("jdk.CPULoad").withPeriod(JDK_EVENT_PROCESSING_PERIOD);
        recording.onEvent("jdk.CPULoad", JfrAgentMonitor::onCpuLoad);
    }

    private static void onGcConfiguration(RecordedEvent event) {

        log.info("JFR Agent - onGcConfiguration -> oldCollector: {} youngCollector: {}",
                event.getString("oldCollector"),
                event.getString("youngCollector"));
    }

    private static void onGcHeapSummary(RecordedEvent event) {

        log.info("JFR Agent - onGcHeapSummary -> heapUsed: {} heapSpace.committedSize: {}",
                event.getLong("heapUsed"),
                event.getLong("heapSpace.committedSize"));
    }

    private static void onCpuLoad(RecordedEvent event) {

        log.info("JFR Agent - onCpuLoad -> machineTotal: {} jvmSystem: {} jvmUser: {}",
                event.getDouble("machineTotal"),
                event.getDouble("jvmSystem"),
                event.getDouble("jvmUser"));
    }

    private static void onJavaThreadStatistics(RecordedEvent event) {

        log.info("JFR Agent - onJavaThreadStatistics -> activeCount: " + event.getValue("activeCount"));
    }
}
