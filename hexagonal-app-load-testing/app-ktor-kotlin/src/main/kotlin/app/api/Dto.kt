data class WorkCpuRequest(val inputs: List<String> = emptyList())

data class BlockingResponse(val success: Boolean)

class WorkCpuResponse(nanoseconds: Long?) {

    var nanoseconds: Long? = null
    var milliseconds: Long? = null
    var seconds: Int? = null

    init {
        this.nanoseconds = nanoseconds
        if (nanoseconds != null) {
            val millis = nanoseconds / 1_000_000
            milliseconds = millis
            seconds = (millis / 1_000).toInt()
        }
    }
}

