package app

import app.JsonMapper.mapper
import app.api.files
import app.api.observe
import app.api.respondError
import app.api.roots
import app.exception.ResponseStatus
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.resource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.JacksonConverter
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.slf4j.event.Level
import java.util.concurrent.TimeUnit

private var server: NettyApplicationEngine? = null

@ExperimentalCoroutinesApi
fun main() {
    server = embeddedServer(Netty,
        port = 8080,
        module = Application::mainModule,
        configure = {
            // Timeout in seconds for sending responses to client
            responseWriteTimeoutSeconds = 20
        }).start()
}

fun shutdown() {
    server?.stop(0, 0, TimeUnit.MILLISECONDS)
}

@ExperimentalCoroutinesApi
internal fun Application.mainModule() {

    install(DefaultHeaders) {
        header(HttpHeaders.Server, "Hidden")
    }
    install(CallLogging) {
        level = Level.INFO
    }
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(mapper))
    }
    install(StatusPages) {
        exception<ResponseStatus> { cause ->
            call.respondError(environment, cause, cause.statusCode)
        }
        exception<Throwable> { cause ->
            call.respondError(environment, cause)
        }
    }

    // TODO: enable-disable by env variable as it is only needed for development in local
    install(AutoHeadResponse)
    install(CORS) {
        anyHost()
        header(HttpHeaders.ContentType)
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Delete)
    }

    routing {
        roots()
        files()
        observe()

        static {
            resource("/", "/static/index.html")
            resource("/tree", "/static/index.html")
            resource("/manifest.json", "/static/manifest.json")
            resource("/favicon.ico", "/static/favicon.ico")

            static("/static") {
                resources("static/static")
            }
        }
    }
}
