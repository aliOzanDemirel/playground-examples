package app.api

import business.service.CpuBoundUseCase
import business.service.dto.WorkCpuCommand
import org.slf4j.LoggerFactory
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.core.Response

@Path("/cpu")
@ApplicationScoped
class CpuHandler {

    @Inject
    lateinit var cpuBoundUseCase: CpuBoundUseCase

    companion object {
        private val log = LoggerFactory.getLogger(CpuHandler::class.java)
    }

    @POST
    fun cpu(@Valid workCpuRequest: WorkCpuRequest): Response {

        val body = compute(workCpuRequest.inputs)
        return Response.accepted(body).build()
    }

    fun compute(inputs: List<String>): WorkCpuResponse {

        log.debug("CPU task with {} inputs", inputs.size)

        val command = WorkCpuCommand(inputs)
        val durationInNanos = cpuBoundUseCase.workCpu(command)
        return WorkCpuResponse(durationInNanos)
    }
}