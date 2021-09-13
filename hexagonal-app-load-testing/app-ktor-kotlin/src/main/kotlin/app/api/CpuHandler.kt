package app.api

import WorkCpuRequest
import WorkCpuResponse
import app.DependencyFactory
import business.dto.CpuWorkInputResponse
import business.entity.CpuTask
import business.service.CpuBoundUseCase
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

            val task = CpuTask.Builder(requestBody.inputs).build()
            val responses = cpuBoundUseCase.compute(task)
            val durationInNanos = responses.stream().mapToLong { it: CpuWorkInputResponse -> it.inputProcessingDuration }.sum()
            call.respond(HttpStatusCode.Accepted, WorkCpuResponse(durationInNanos))
        }
    }
}