package app.api

import WorkCpuRequest
import WorkCpuResponse
import app.DependencyFactory
import business.service.CpuBoundUseCase
import business.service.dto.WorkCpuCommand
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("app.CpuHandler")

internal fun Routing.cpuHandler() {

    val cpuBoundUseCase: CpuBoundUseCase = DependencyFactory.cpuBoundUseCase()

    post("/cpu") {

        val requestBody = call.receive(WorkCpuRequest::class)
        if (requestBody.inputs.isEmpty()) {
            throw BadRequestException("CPU work inputs cannot be empty!")
        }

        withContext(Dispatchers.Unconfined) {

            log.debug("CPU task with {} inputs", requestBody.inputs.size)

            val command = WorkCpuCommand(requestBody.inputs)
            val duration = cpuBoundUseCase.workCpu(command)
            call.respond(HttpStatusCode.Accepted, WorkCpuResponse(duration))
        }
    }
}