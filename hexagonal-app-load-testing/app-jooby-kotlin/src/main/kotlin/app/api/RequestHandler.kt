package app.api

import WorkCpuRequest
import app.service.AppJoobyService
import io.jooby.HandlerContext
import io.jooby.Route
import io.jooby.StatusCode
import io.jooby.exception.BadRequestException
import org.koin.java.KoinJavaComponent

object RequestHandler {

    private val appJoobyService by KoinJavaComponent.inject<AppJoobyService>(AppJoobyService::class.java)

    val blockingHandler = Route.Handler {

        val duration = it.queryMap()["duration"] ?: "1000"
        val response = appJoobyService.threadBlockingIo(duration.toLong())
        it.render(response)
    }

    val suspendingHandler: suspend (handlerCtx: HandlerContext) -> Any = {

        val duration = it.ctx.queryMap()["duration"] ?: "1000"
        val response = appJoobyService.coroutineSuspendingIo(duration.toLong())
        it.ctx.render(response)
    }

    val cpuHandler = Route.Handler {

        val body: WorkCpuRequest? = it.body(WorkCpuRequest::class.java)
        if (body?.inputs == null || body.inputs.isEmpty()) {
            throw BadRequestException("CPU work inputs cannot be empty!")
        }

        it.responseCode = StatusCode.ACCEPTED
        val response = appJoobyService.compute(body.inputs)
        it.render(response)
    }
}