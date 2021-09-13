package app.api;

import app.dto.BlockingResponse;
import business.entity.IoTask;
import business.service.IoBoundUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(IoHandler.class);

    private final IoBoundUseCase ioBoundUseCase;

    @Inject
    public IoHandler(IoBoundUseCase ioBoundUseCase) {
        this.ioBoundUseCase = ioBoundUseCase;
    }

    @GET
    public BlockingResponse io(@QueryParam("duration") Optional<Long> duration) {

        Long durationInt = duration.orElse(1000L);

        log.debug("IO task with blocking thread, duration {}", durationInt);

        var ioTask = new IoTask.Builder<>(UUID.randomUUID(), IoTask.defaultBlockingBehaviour())
                .duration(durationInt)
                .build();
        return new BlockingResponse(ioBoundUseCase.run(ioTask));
    }
}