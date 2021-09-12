package app.api

import BlockingResponse
import app.DependencyFactory
import business.entity.IoTask
import business.service.IoBoundUseCase
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.*
import java.util.function.Function

private val log = LoggerFactory.getLogger("app.IoHandler")

internal fun Routing.ioHandler() {

    val ioBoundUseCase: IoBoundUseCase = DependencyFactory.ioBoundUseCase()

    get("/io") {

        val durationParam = this.context.request.queryParameters["duration"]
        val duration: Long = if (durationParam.isNullOrBlank()) 1000 else durationParam.toLong()
        if (duration < 1000) {
            throw BadRequestException("Duration should be positive number!")
        }

        withContext(Dispatchers.IO) {

            log.debug("IO task with suspended coroutine, duration: {}", duration)

            val id = UUID.randomUUID()
            val task = IoTask.IoTaskBuilder(id, nonBlockingAsyncBehaviour)
                .duration(duration)
                .build()

            val result = ioBoundUseCase.run(task)
            call.respond(BlockingResponse(result.await()))
        }
    }
}

val nonBlockingAsyncBehaviour = Function<Long, Deferred<Boolean>> {
    GlobalScope.async {
        delay(it)
        true
    }
}