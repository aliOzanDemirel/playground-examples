package app.api;

import app.util.Util;
import business.entity.IoTask;
import business.service.IoBoundUseCase;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class BlockingIoHandler implements Handler<RoutingContext> {

    private static final Logger log = LoggerFactory.getLogger(BlockingIoHandler.class);

    private final IoBoundUseCase ioBoundUseCase;

    public BlockingIoHandler(IoBoundUseCase ioBoundUseCase) {
        this.ioBoundUseCase = ioBoundUseCase;
    }

    @Override
    public void handle(RoutingContext routingCtx) {

        long duration = Util.readDurationQueryParam(routingCtx);
        if (duration > 0) {
            blockingIo(routingCtx, duration);
        }
    }

    private void blockingIo(RoutingContext routingCtx, long duration) {

        log.debug("IO task with delegated blocking thread, duration {}", duration);

        var id = UUID.randomUUID();
        var ioBehaviour = IoTask.defaultBlockingBehaviour();
        var task = new IoTask.IoTaskBuilder<>(id, ioBehaviour)
                .duration(duration)
                .build();

        Boolean result = ioBoundUseCase.run(task);
        Util.sendIoResponse(routingCtx, result);
    }
}
