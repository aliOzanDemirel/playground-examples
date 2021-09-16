package agent.clothing;

import agent.influx.InfluxWriter;
import org.influxdb.dto.Point;

public class ClothingServiceMetricWriter {

    private static final String CLOTHING_REVIEW_MEASUREMENT_NAME = "clothing_reviews";

    public void writeReviewAddedEvent(int isSuccess, int rating, String clothingBrand) {

        String brand = clothingBrand == null ? "unknown" : clothingBrand;
        String requestStatus = "unknown";
        if (isSuccess == 1) {
            requestStatus = "success";
        } else if (isSuccess == 0) {
            requestStatus = "failure";
        }

        Point.Builder dataPointBuilder = Point.measurement(CLOTHING_REVIEW_MEASUREMENT_NAME)
                .addField("rating", rating)
                .tag("request_status", requestStatus)
                .tag("brand", brand);

        InfluxWriter.getWriter().writeDataPoint(dataPointBuilder);
    }
}
