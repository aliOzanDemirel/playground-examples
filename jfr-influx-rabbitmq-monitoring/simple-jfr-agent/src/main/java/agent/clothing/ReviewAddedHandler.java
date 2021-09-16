package agent.clothing;

import agent.util.EventUtil;
import jdk.jfr.consumer.RecordedEvent;

import java.util.function.Consumer;

import static agent.util.EventUtil.NUMERIC_UNKNOWN_ID;

public class ReviewAddedHandler implements Consumer<RecordedEvent> {

    private final ClothingServiceMetricWriter writer = new ClothingServiceMetricWriter();

    @Override
    public void accept(RecordedEvent event) {

        EventUtil.logEventDuration(event, getClass());

        int isSuccess = NUMERIC_UNKNOWN_ID;
        if (event.hasField("isSuccess")) {
            boolean success = event.getValue("isSuccess");
            isSuccess = success ? 1 : 0;
        }
        int rating = NUMERIC_UNKNOWN_ID;
        if (event.hasField("rating")) {
            rating = event.getValue("rating");
        }
        String clothingBrand = null;
        if (event.hasField("clothingBrand")) {
            clothingBrand = event.getValue("clothingBrand");
        }

        writer.writeReviewAddedEvent(isSuccess, rating, clothingBrand);
    }
}
