package app.api

import business.entity.IoTask
import business.service.IoBoundUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.scheduling.TaskExecutors
import jakarta.inject.Named
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.function.Function

@Controller
class IoController(
    private val ioBoundUseCase: IoBoundUseCase,
    @Named(TaskExecutors.IO) private val ioExecutors: ExecutorService
) {

    companion object {
        private val log = LoggerFactory.getLogger(IoController::class.java)
    }

    @Get("/io")
    suspend fun io(@QueryValue(defaultValue = "1000") duration: Long): IoResponse = withContext(ioExecutors.asCoroutineDispatcher()) {

        log.info("PROP: {}", System.getProperty("kotlinx.coroutines.debug"))
        log.info("PROP 2: {}", System.getProperty("deneme"))

        nonBlockingIo(duration)
    }

    suspend fun nonBlockingIo(duration: Long): IoResponse {

        log.debug("IO task with suspended coroutine, duration: {}", duration)

        val currentCoroutineContext = currentCoroutineContext()
        val nonBlockingCoroutineBehaviour = Function<Long, Boolean> {
            try {
                CoroutineScope(context = currentCoroutineContext).launch {
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
        return IoResponse(ioBoundUseCase.run(task))
    }
}