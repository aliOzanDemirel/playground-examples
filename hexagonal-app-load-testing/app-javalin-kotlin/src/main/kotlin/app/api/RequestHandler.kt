package app.api

import app.service.AppJavalinService
import io.javalin.http.Handler
import org.eclipse.jetty.http.HttpStatus
import org.koin.java.KoinJavaComponent.inject

object RequestHandler {

    private val appJavalinService by inject<AppJavalinService>(AppJavalinService::class.java)

    val io: Handler = Handler { ctx ->

        // assert non null with !!
        val duration = ctx.queryParam("duration", "1000")!!
        val response = appJavalinService.io(duration.toLong())
        ctx.json(response)
    }

    val cpu: Handler = Handler { ctx ->

        val requestBody = ctx.bodyValidator<WorkCpuRequest>().check(
            { it.inputs.isNotEmpty() }, "CPU work inputs cannot be empty!"
        ).get()
        val response = appJavalinService.compute(requestBody.inputs)

        ctx.res.status = HttpStatus.ACCEPTED_202
        ctx.json(response)
    }
}