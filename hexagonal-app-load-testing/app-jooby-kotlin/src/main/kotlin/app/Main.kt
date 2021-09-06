package app

import app.api.RequestHandler.blockingHandler
import app.api.RequestHandler.cpuHandler
import app.api.RequestHandler.suspendingHandler
import app.config.appModule
import app.config.businessModule
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.jooby.AccessLogHandler
import io.jooby.ExecutionMode
import io.jooby.StatusCode.BAD_REQUEST
import io.jooby.json.JacksonModule
import io.jooby.runApp
import org.koin.core.context.startKoin

fun main(args: Array<String>) {

    runApp(args, ExecutionMode.WORKER) {

        // register jackson's kotlin module to jooby's object mapper
        install(JacksonModule())
        val mapper: ObjectMapper = require(ObjectMapper::class.java)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.registerKotlinModule()

        // setup DI context
        startKoin { modules(businessModule, appModule) }

        // load configuration files from classpath by given name
        val env = environmentOptions {
            filename = "app.properties"
        }

        // enable/disable web server access logging
        if (env.getProperty("requestLoggingEnabled") == "true") {
            log.info("Enabling request logging...")
            decorator(AccessLogHandler())
        }

        val isThreadPerRequest = env.getProperty("threadPerRequest") == "true"
        if (isThreadPerRequest) {

            log.info("Worker thread per request is enabled.")

            // configure dispatched thread
            get("/io", blockingHandler)
            post("/cpu", cpuHandler)
        } else {

            log.info("Coroutine dispatching for request processing is enabled.")

            // configure dispatched coroutine
            coroutine {
                get("/io", suspendingHandler)
                post("/cpu", cpuHandler)
            }
        }

        // do not send html error page, just status code
        error(BAD_REQUEST) { ctx, cause, statusCode ->
            ctx.send(statusCode)
        }
    }
}