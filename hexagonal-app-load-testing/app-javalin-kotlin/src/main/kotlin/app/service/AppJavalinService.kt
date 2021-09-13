package app.service

import app.api.BlockingResponse
import app.api.WorkCpuResponse
import business.dto.CpuWorkInputResponse
import business.entity.CpuTask
import business.entity.IoTask
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import org.slf4j.LoggerFactory
import java.util.*

class AppJavalinService(
    private val ioBoundUseCase: IoBoundUseCase,
    private val cpuBoundUseCase: CpuBoundUseCase
) {

    companion object {
        private val log = LoggerFactory.getLogger(AppJavalinService::class.java)
    }

    fun compute(inputs: List<String>): WorkCpuResponse {

        log.debug("CPU task with {} inputs", inputs.size)

        val task = CpuTask.Builder(inputs).build()
        val responses = cpuBoundUseCase.compute(task)
        val durationInNanos = responses.stream().mapToLong { it: CpuWorkInputResponse -> it.inputProcessingDuration }.sum()
        return WorkCpuResponse(durationInNanos)
    }

    fun io(duration: Long): BlockingResponse {

        log.debug("IO task with blocked thread, duration: {}", duration)

        val id = UUID.randomUUID()
        val task = IoTask.Builder(id, IoTask.defaultBlockingBehaviour()).duration(duration).build()
        return BlockingResponse(ioBoundUseCase.run(task))
    }
}