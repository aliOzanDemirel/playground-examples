package app.service

import app.api.BlockingResponse
import app.api.WorkCpuResponse
import app.api.withNanos
import business.entity.IoTask
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import business.service.dto.WorkCpuCommand
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.*
import java.util.function.Function

@Service
class AppSpringService(
    private val ioBoundUseCase: IoBoundUseCase,
    private val cpuBoundUseCase: CpuBoundUseCase
) {

    companion object {
        private val log = LoggerFactory.getLogger(AppSpringService::class.java)
    }

    fun compute(inputs: List<String>): WorkCpuResponse {

        log.debug("CPU task with {} inputs", inputs.size)

        val command = WorkCpuCommand(inputs)
        val durationInNanos = cpuBoundUseCase.workCpu(command)
        return WorkCpuResponse().withNanos(durationInNanos)
    }

    fun threadBlockingIo(duration: Long): BlockingResponse {

        log.debug("IO task with blocked thread, duration: {}", duration)

        val id = UUID.randomUUID()
        val task = IoTask.IoTaskBuilder(id, IoTask.defaultBlockingBehaviour())
            .duration(duration)
            .build()
        return BlockingResponse(ioBoundUseCase.run(task))
    }

    fun reactorDelayingIo(duration: Long): Mono<BlockingResponse> {

        log.debug("IO task with asyncThreadBlockingIo, duration: {}", duration)

        val lazyMonoDelayingBehaviour = Function<Long, Mono<Boolean>> {

            Mono.just(true)
                .onErrorReturn(false)
                .delayElement(Duration.ofMillis(it), Schedulers.boundedElastic())
        }

        val id = UUID.randomUUID()
        val task: IoTask<Mono<Boolean>> = IoTask.IoTaskBuilder(id, lazyMonoDelayingBehaviour)
            .duration(duration)
            .build()

        return Mono.just(task)
            .map { ioBoundUseCase.run(task) }
            .flatMap { it }
            .publishOn(Schedulers.boundedElastic())
            .map { BlockingResponse(it) }
            .subscribeOn(Schedulers.boundedElastic())
    }

    // non-blocking without async constructs like future, promise etc.
    suspend fun coroutineSuspendingIo(duration: Long): BlockingResponse {

        log.debug("IO task with suspended coroutine, duration: {}", duration)

        val currentCoroutineContext = currentCoroutineContext()
        val nonBlockingCoroutineBehaviour = Function<Long, Boolean> {
            try {
                CoroutineScope(context = currentCoroutineContext).launch(Dispatchers.IO) {
                    delay(it)
                }
                true
            } catch (e: Exception) {
                false
            }
        }

        val id = UUID.randomUUID()
        val task = IoTask.IoTaskBuilder(id, nonBlockingCoroutineBehaviour)
            .duration(duration)
            .build()
        return BlockingResponse(ioBoundUseCase.run(task))
    }
}