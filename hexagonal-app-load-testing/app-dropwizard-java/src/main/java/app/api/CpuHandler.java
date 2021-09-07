package app.api;

import app.dto.WorkCpuRequest;
import app.dto.WorkCpuResponse;
import business.service.CpuBoundUseCase;
import business.service.dto.WorkCpuCommand;

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

    private final CpuBoundUseCase cpuBoundUseCase;

    @Inject
    public CpuHandler(CpuBoundUseCase cpuBoundUseCase) {
        this.cpuBoundUseCase = cpuBoundUseCase;
    }

    @POST
    public Response cpu(@Valid @NotNull WorkCpuRequest request) {

        var command = new WorkCpuCommand(request.getInputs());
        Long durationInNanos = cpuBoundUseCase.workCpu(command);

        return Response.status(Response.Status.ACCEPTED)
                .entity(new WorkCpuResponse(durationInNanos))
                .build();
    }
}