package app.service

import BlockingResponse
import WorkCpuResponse
import business.dto.CpuWorkInputResponse
import business.entity.CpuTask
import business.entity.IoTask
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.util.*
import java.util.function.Function

class AppJoobyService(
    private val ioBoundUseCase: IoBoundUseCase,
    private val cpuBoundUseCase: CpuBoundUseCase
) {

    companion object {
        private val log = LoggerFactory.getLogger(AppJoobyService::class.java)
    }

    fun compute(inputs: List<String>): WorkCpuResponse {

        log.debug("CPU task with {} inputs", inputs.size)

        val task = CpuTask.Builder(inputs).build()
        val responses = cpuBoundUseCase.compute(task)
        val durationInNanos = responses.stream().mapToLong { it: CpuWorkInputResponse -> it.inputProcessingDuration }.sum()
        return WorkCpuResponse(durationInNanos)
    }

    fun threadBlockingIo(duration: Long): BlockingResponse {

        log.debug("IO task with blocked thread, duration: {}", duration)

        val id = UUID.randomUUID()
        val task = IoTask.Builder(id, IoTask.defaultBlockingBehaviour())
            .duration(duration)
            .build()
        return BlockingResponse(ioBoundUseCase.run(task))
    }

    suspend fun coroutineSuspendingIo(duration: Long): BlockingResponse {

        log.debug("IO task with suspended coroutine, duration: {}", duration)

        val nonBlockingCoroutineBehaviour = Function<Long, Deferred<Boolean>> { appliedDuration ->
            GlobalScope.async {
                try {
                    delay(appliedDuration)
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }

        val id = UUID.randomUUID()
        val task = IoTask.Builder(id, nonBlockingCoroutineBehaviour)
            .duration(duration)
            .build()

        val async: Deferred<Boolean> = ioBoundUseCase.run(task)
        return BlockingResponse(async.await())
    }
}