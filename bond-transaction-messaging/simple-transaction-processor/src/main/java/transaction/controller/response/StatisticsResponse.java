package transaction.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@JsonPropertyOrder({"sum", "avg", "max", "min", "count"})
public class StatisticsResponse {

    private long count = 0;

    @JsonIgnore
    private BigDecimal sumD = BigDecimal.ZERO;
    @JsonIgnore
    private BigDecimal avgD = BigDecimal.ZERO;
    @JsonIgnore
    private BigDecimal maxD = BigDecimal.ZERO;
    @JsonIgnore
    private BigDecimal minD = BigDecimal.ZERO;

    /**
     * calculates avg and sets scale for all values
     */
    public StatisticsResponse finalizeStats(int scale) {

        sumD = sumD.setScale(scale, RoundingMode.HALF_UP);
        maxD = maxD.setScale(scale, RoundingMode.HALF_UP);
        minD = minD.setScale(scale, RoundingMode.HALF_UP);

        if (count != 0) {
            avgD = sumD.divide(BigDecimal.valueOf(count), RoundingMode.HALF_UP);
        }
        avgD = avgD.setScale(2, RoundingMode.HALF_UP);

        return this;
    }

    // getters for jackson to serialize string objects
    public String getSum() {
        return sumD.toString();
    }

    public String getAvg() {
        return avgD.toString();
    }

    public String getMax() {
        return maxD.toString();
    }

    public String getMin() {
        return minD.toString();
    }

}
