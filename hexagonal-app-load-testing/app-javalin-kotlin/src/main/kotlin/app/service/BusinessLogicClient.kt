package app.service

import app.api.BlockingResponse
import app.api.WorkCpuResponse
import business.entity.IoTask
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import business.service.dto.WorkCpuCommand
import org.slf4j.LoggerFactory
import java.util.*

class BusinessLogicClient(
    private val ioBoundUseCase: IoBoundUseCase,
    private val cpuBoundUseCase: CpuBoundUseCase
) {

    companion object {
        private val log = LoggerFactory.getLogger(BusinessLogicClient::class.java)
    }

    fun workCpu(inputs: List<String>): WorkCpuResponse {

        log.info("Computing with {} inputs", inputs.size)

        val command = WorkCpuCommand(inputs)
        val durationInNanos = cpuBoundUseCase.workCpu(command)
        return WorkCpuResponse(durationInNanos)
    }

    fun blockingIo(duration: Int): BlockingResponse {

        log.info("IO with simulated duration {}", duration)

        val id = UUID.randomUUID()
        val task = IoTask.IoTaskBuilder(id).duration(duration).build()
        return BlockingResponse(ioBoundUseCase.block(task))
    }
}