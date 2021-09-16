package agent.util;

import jdk.jfr.consumer.RecordedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventUtil {

    public static final int NUMERIC_UNKNOWN_ID = -1;
    private static final Logger log = LoggerFactory.getLogger(EventUtil.class);

    private EventUtil() {
    }

    public static void logEventDuration(RecordedEvent event, Class consumer) {

        int nano = event.getDuration().getNano();
        int ms = nano / 1_000_000;
        log.info("Event is caught by {}, duration nano: {} ms: {}", consumer, nano, ms);
    }
}
