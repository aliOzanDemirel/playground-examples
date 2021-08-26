package app.service

import app.dto.BlockingResponse
import app.dto.WorkCpuResponse
import business.entity.IoTask
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import business.service.dto.WorkCpuCommand
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class BusinessLogicClient(
    private val ioBoundUseCase: IoBoundUseCase,
    private val cpuBoundUseCase: CpuBoundUseCase
) {

    private val log = LoggerFactory.getLogger(BusinessLogicClient::class.java)

    fun workCpu(inputs: List<String>): WorkCpuResponse {

        log.debug("Computing with {} inputs", inputs.size)

        val command = WorkCpuCommand(inputs)
        val durationInNanos = cpuBoundUseCase.workCpu(command)

        if (durationInNanos == null) {
            return WorkCpuResponse(null, null, null)
        }
        val durationInMillis = durationInNanos / 1_000_000
        val durationInSeconds = (durationInMillis / 1_000).toInt()
        return WorkCpuResponse(durationInNanos, durationInMillis, durationInSeconds)
    }

    fun blockingIo(duration: Int): BlockingResponse {

        log.debug("IO with simulated duration {}", duration)

        val id = UUID.randomUUID()
        val task = IoTask.IoTaskBuilder(id).duration(duration).build()
        return BlockingResponse(ioBoundUseCase.block(task))
    }
}