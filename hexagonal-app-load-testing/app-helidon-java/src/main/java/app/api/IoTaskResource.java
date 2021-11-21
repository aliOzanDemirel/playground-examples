package app.api;

import app.dto.BlockingResponse;
import business.entity.IoTask;
import business.service.IoBoundUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/io")
@RequestScoped
public class IoTaskResource {

    private static final Logger log = LoggerFactory.getLogger(IoTaskResource.class);
    private final IoBoundUseCase ioBoundUseCase;

    @Inject
    public IoTaskResource(IoBoundUseCase ioBoundUseCase) {
        this.ioBoundUseCase = ioBoundUseCase;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public BlockingResponse io(@QueryParam("duration") Long duration) {

        if (duration == null) {
            duration = 1000L;
        }

        log.debug("IO task with blocking thread, duration {}", duration);

        var ioTask = new IoTask.Builder<>(IoTask.defaultBlockingBehaviour())
                .duration(duration)
                .build();
        return new BlockingResponse(ioBoundUseCase.run(ioTask));
    }
}