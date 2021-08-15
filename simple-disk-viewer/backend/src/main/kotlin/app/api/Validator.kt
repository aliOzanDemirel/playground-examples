package app.api

import app.exception.BadRequest
import io.ktor.application.ApplicationCall

internal fun ApplicationCall.paramShouldExist(paramName: String): String {

    val value = this.request.queryParameters[paramName]
    if (value.isNullOrBlank()) {
        throw BadRequest("Parameter '$paramName' is missing!")
    }
    return value
}

internal fun ApplicationCall.paramShouldBePositive(paramName: String): Int {

    val intVal = paramShouldExist(paramName).toInt()
    if (intVal <= 0) {
        throw BadRequest("Parameter '$paramName' should be positive number")
    }
    return intVal
}

internal fun ApplicationCall.paramIntIfExists(paramName: String): Int? {

    return this.request.queryParameters[paramName]?.toInt()
}
