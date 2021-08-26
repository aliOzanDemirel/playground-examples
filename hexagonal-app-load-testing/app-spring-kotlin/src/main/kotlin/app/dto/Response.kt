package app.dto

data class BlockingResponse(val success: Boolean)
data class WorkCpuResponse(val nanoseconds: Long?, val milliseconds: Long?, val seconds: Int?)