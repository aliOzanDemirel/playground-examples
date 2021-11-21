package app.dto;

public class WorkCpuResponse {

    public WorkCpuResponse(Long nanoseconds) {

        if (nanoseconds != null) {

            this.nanoseconds = nanoseconds;
            milliseconds = nanoseconds / 1_000_000;
            seconds = (int) (milliseconds / 1000);
        }
    }

    private Long nanoseconds;
    private Long milliseconds;
    private Integer seconds;

    public Long getNanoseconds() {
        return nanoseconds;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public Integer getSeconds() {
        return seconds;
    }
}
