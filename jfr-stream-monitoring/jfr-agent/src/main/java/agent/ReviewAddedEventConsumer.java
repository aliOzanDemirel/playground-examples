package agent;

import jdk.jfr.consumer.RecordedEvent;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class ReviewAddedEventConsumer implements Consumer<RecordedEvent> {

    private static final Logger javaLog = Logger.getLogger("ReviewAddedEventConsumer");
    private final InfluxWriter influxWriter = new InfluxWriter();

    @Override
    public void accept(RecordedEvent event) {

        int nano = event.getDuration().getNano();
        int ms = nano / 1_000_000;
        javaLog.info("ReviewAddedEvent is caught, duration nano: " + nano + " ms: " + ms);

        int isSuccess = -1;
        if (event.hasField("isSuccess")) {
            boolean success = event.getValue("isSuccess");
            isSuccess = success ? 1 : 0;
        }
        int rating = -1;
        if (event.hasField("rating")) {
            rating = event.getValue("rating");
        }
        String clothingBrand = null;
        if (event.hasField("clothingBrand")) {
            clothingBrand = event.getValue("clothingBrand");
        }

        influxWriter.writeReviewAddedEvent(isSuccess, rating, clothingBrand);
    }
}
