package app.exception

import io.ktor.http.HttpStatusCode

open class ResponseStatus(message: String, val statusCode: HttpStatusCode, cause: Exception?) : Exception(message, cause)

class BadRequest(message: String, cause: Exception? = null) : ResponseStatus(message, HttpStatusCode.BadRequest, cause)

class AccessDenied(message: String, cause: Exception) : ResponseStatus(message, HttpStatusCode.Unauthorized, cause)

class NotFound(message: String, cause: Exception) : ResponseStatus(message, HttpStatusCode.NotFound, cause)
