package app.dto

class WorkCpuResponse {

    WorkCpuResponse(Long nanoseconds) {

        if (nanoseconds != null) {

            this.nanoseconds = nanoseconds
            milliseconds = nanoseconds.intdiv(1_000_000)
            seconds = milliseconds.intdiv(1_000).intValue()
        }
    }

    Long nanoseconds
    Long milliseconds
    Integer seconds
}
