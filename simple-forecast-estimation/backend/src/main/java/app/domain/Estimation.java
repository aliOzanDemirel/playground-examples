package app.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Document
@CompoundIndexes ({
        @CompoundIndex (name = "timeseries_user_spring", unique = true, def = "{'timeseriesUUID' : 1, 'userUUID': 1}")
})
public class Estimation {

    @Id
    private String estimationUUID;
    @NotNull
    private String timeseriesUUID;
    @NotNull
    private String userUUID;
    @NotNull
    @Min(value = 0L, message = "The value must be positive")
    private int estimation;
    private double score;

    public Estimation() {
    }

    public Estimation(String estimationUUID, String timeseriesUUID, String userUUID, int estimation, double score) {
        this.estimationUUID = estimationUUID;
        this.timeseriesUUID = timeseriesUUID;
        this.userUUID = userUUID;
        this.estimation = estimation;
        this.score = score;
    }

    @JsonIgnore
    public void setEstimationUUID(String estimationUUID) {
        this.estimationUUID = estimationUUID;
    }

    @JsonProperty
    public String getEstimationUUID() {
        return estimationUUID;
    }

    public String getTimeseriesUUID() {
        return timeseriesUUID;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public int getEstimation() {
        return estimation;
    }

    @JsonProperty
    public double getScore() {
        return score;
    }

    @JsonIgnore
    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Estimation{" +
                "estimationUUID=" + estimationUUID +
                ", timeseriesUUID=" + timeseriesUUID +
                ", userUUID=" + userUUID +
                ", estimation=" + estimation +
                ", score=" + score +
                '}';
    }
}
