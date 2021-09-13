package app.api

import business.dto.CpuWorkInputResponse
import business.entity.CpuTask
import business.service.CpuBoundUseCase
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

        val task = CpuTask.Builder(inputs).build()
        val responses = cpuBoundUseCase.compute(task)
        val durationInNanos = responses.stream().mapToLong { it: CpuWorkInputResponse -> it.inputProcessingDuration }.sum()
        return WorkCpuResponse(durationInNanos)
    }
}