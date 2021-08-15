package app.api

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationEnvironment
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

suspend fun ApplicationCall.respondError(
    environment: ApplicationEnvironment,
    cause: Throwable,
    statusCode: HttpStatusCode = HttpStatusCode.InternalServerError
) {
    environment.log.error("{}", cause)
    this.respond(
        statusCode, mapOf(
            "request" to this.request.local.uri,
            "message" to cause.message
        )
    )
}

