package app.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
@JsonIgnoreProperties(value = { "part" })
public class Timeseries {

    @Id
    private String timeseriesUUID;
    private Instant startDate;
    private Long sequence;
    private int sunCoverage;
    private int windSpeed;
    private int power;
    private double total;
    private TimeseriesStatus status;

    public Timeseries() {
    }

    public Timeseries(String timeseriesUUID, Instant startDate, Long sequence, int sunCoverage, int windSpeed, int power, double total, TimeseriesStatus status) {
        this.timeseriesUUID = timeseriesUUID;
        this.startDate = startDate;
        this.sequence = sequence;
        this.sunCoverage = sunCoverage;
        this.windSpeed = windSpeed;
        this.power = power;
        this.total = total;
        this.status = status;
    }

    @JsonIgnore
    public void setTimeseriesUUID(String timeseriesUUID) {
        this.timeseriesUUID = timeseriesUUID;
    }

    @JsonProperty
    public String getTimeseriesUUID() {
        return timeseriesUUID;
    }

    @JsonIgnore
    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    @JsonProperty
    public Instant getStartDate() {
        return startDate;
    }

    @JsonProperty("sequence")
    public Long getSequence() {
        return sequence;
    }

    @JsonProperty("day")
    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    @JsonIgnore
    public void setSunCoverage(int sunCoverage) {
        this.sunCoverage = sunCoverage;
    }

    @JsonProperty
    public int getSunCoverage() {
        return sunCoverage;
    }

    @JsonIgnore
    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    @JsonProperty
    public int getWindSpeed() {
        return windSpeed;
    }

    public int getPower() {
        return power;
    }

    @JsonIgnore
    public void setTotal(double total) {
        this.total = total;
    }

    @JsonProperty
    public double getTotal() {
        return total;
    }

    public TimeseriesStatus getStatus() {
        return status;
    }

    public void setStatus(TimeseriesStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Timeseries{" +
                "timeseriesUUID='" + timeseriesUUID + '\'' +
                ", startDate=" + startDate +
                ", sequence=" + sequence +
                ", sunCoverage=" + sunCoverage +
                ", windSpeed=" + windSpeed +
                ", power=" + power +
                ", total=" + total +
                ", status=" + status +
                '}';
    }
}
