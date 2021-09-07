package app.api;

import app.dto.BlockingResponse;
import business.entity.IoTask;
import business.service.IoBoundUseCase;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.UUID;

@Path("/io")
@Produces(MediaType.APPLICATION_JSON)
public class IoHandler {

    private final IoBoundUseCase ioBoundUseCase;

    @Inject
    public IoHandler(IoBoundUseCase ioBoundUseCase) {
        this.ioBoundUseCase = ioBoundUseCase;
    }

    @GET
    public BlockingResponse io(@QueryParam("duration") Optional<Long> duration) {

        Long durationInt = duration.orElse(1000L);

        var ioTask = new IoTask.IoTaskBuilder<>(UUID.randomUUID(), IoTask.defaultBlockingBehaviour())
                .duration(durationInt)
                .build();
        return new BlockingResponse(ioBoundUseCase.run(ioTask));
    }
}