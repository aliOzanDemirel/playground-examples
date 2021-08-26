package app.api

import app.service.BusinessLogicClient
import io.javalin.http.Handler
import org.eclipse.jetty.http.HttpStatus
import org.koin.java.KoinJavaComponent.inject

object RequestHandler {

    private val businessLogicClient by inject<BusinessLogicClient>(BusinessLogicClient::class.java)

    val io: Handler = Handler { ctx ->

        // assert non null with !!
        val duration = ctx.queryParam("duration", "1000")!!
        val response = businessLogicClient.blockingIo(duration.toInt())
        ctx.json(response)
    }

    val cpu: Handler = Handler { ctx ->

        val requestBody = ctx.bodyValidator<WorkCpuRequest>().check(
            { it.inputs.isNotEmpty() }, "CPU work inputs cannot be empty!"
        ).get()
        val response = businessLogicClient.workCpu(requestBody.inputs)

        ctx.res.status = HttpStatus.ACCEPTED_202
        ctx.json(response)
    }
}