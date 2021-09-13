package app.api

import business.dto.CpuWorkInputResponse
import business.entity.CpuTask
import business.service.CpuBoundUseCase
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.validation.Valid

// equivalent of spring's async dispatching as netty's event loop threads delegate work to this fixed worker pool
@ExecuteOn("worker")
// so that kotlin plugin makes methods public if they have argument validations
@Validated
@Controller
class CpuController(
    private val cpuBoundUseCase: CpuBoundUseCase
) {

    companion object {
        private val log = LoggerFactory.getLogger(CpuController::class.java)
    }

    @Post("/cpu")
    @Status(HttpStatus.ACCEPTED)
    fun cpu(@Body @Valid workCpuRequest: WorkCpuRequest): WorkCpuResponse {

        return compute(workCpuRequest.inputs)
    }

    fun compute(inputs: List<String>): WorkCpuResponse {

        log.debug("CPU task with {} inputs", inputs.size)

        val task = CpuTask.Builder(inputs).build()
        val responses = cpuBoundUseCase.compute(task)
        val durationInNanos = responses.stream().mapToLong { it: CpuWorkInputResponse -> it.inputProcessingDuration }.sum()
        return WorkCpuResponse(durationInNanos)
    }
}