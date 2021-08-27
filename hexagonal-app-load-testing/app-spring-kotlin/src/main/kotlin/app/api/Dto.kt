package app.api

import javax.validation.constraints.NotEmpty

data class BlockingResponse(val success: Boolean)

data class WorkCpuRequest(@get:NotEmpty val inputs: List<String>)
data class WorkCpuResponse(val nanoseconds: Long?, val milliseconds: Long?, val seconds: Int?) {
    constructor() : this(null, null, null)
}

internal fun WorkCpuResponse.withNanos(durationInNanos: Long?): WorkCpuResponse {
    if (durationInNanos == null) {
        return this
    }
    val durationInMillis = durationInNanos / 1_000_000
    val durationInSeconds = (durationInMillis / 1_000).toInt()
    return WorkCpuResponse(durationInNanos, durationInMillis, durationInSeconds)
}