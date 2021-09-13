package app.api;

import app.dto.WorkCpuRequest;
import app.dto.WorkCpuResponse;
import business.entity.CpuTask;
import business.service.CpuBoundUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/cpu")
@Produces(MediaType.APPLICATION_JSON)
public class CpuHandler {

    private static final Logger log = LoggerFactory.getLogger(CpuHandler.class);

    private final CpuBoundUseCase cpuBoundUseCase;

    @Inject
    public CpuHandler(CpuBoundUseCase cpuBoundUseCase) {
        this.cpuBoundUseCase = cpuBoundUseCase;
    }

    @POST
    public Response cpu(@Valid @NotNull WorkCpuRequest request) {

        log.debug("CPU task with {} inputs", request.getInputs().size());
        var task = new CpuTask.Builder(request.getInputs()).build();
        var responses = cpuBoundUseCase.compute(task);
        long durationInNanos = responses.stream().mapToLong(it -> it.inputProcessingDuration).sum();

        return Response.status(Response.Status.ACCEPTED)
                .entity(new WorkCpuResponse(durationInNanos))
                .build();
    }
}