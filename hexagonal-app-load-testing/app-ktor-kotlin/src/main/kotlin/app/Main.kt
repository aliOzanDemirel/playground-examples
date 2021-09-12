package app

import app.api.cpuHandler
import app.api.ioHandler
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {

    embeddedServer(CIO, port = 8080) {

        install(ContentNegotiation) {
            register(ContentType.Application.Json, JacksonConverter())
        }

        routing {
            ioHandler()
            cpuHandler()
        }

    }.start(wait = true)
}


