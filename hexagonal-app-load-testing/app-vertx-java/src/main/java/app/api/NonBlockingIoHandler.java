package app.api;

import app.util.Util;
import business.entity.IoTask;
import business.service.IoBoundUseCase;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class NonBlockingIoHandler implements Handler<RoutingContext> {

    private static final Logger log = LoggerFactory.getLogger(NonBlockingIoHandler.class);

    private final IoBoundUseCase ioBoundUseCase;
    private final ScheduledExecutorService nonBlockingSimulator;

    public NonBlockingIoHandler(IoBoundUseCase ioBoundUseCase) {
        this.ioBoundUseCase = ioBoundUseCase;
        this.nonBlockingSimulator = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void handle(RoutingContext routingCtx) {

        long duration = Util.readDurationQueryParam(routingCtx);
        if (duration > 0) {
            nonBlockingIo(routingCtx, duration);
        }
    }

    private void nonBlockingIo(RoutingContext routingCtx, long duration) {

        log.debug("IO task with non blocking simulation, duration {}", duration);

        Function<Long, CompletableFuture<Boolean>> nonBlockingIoBehaviour = (Long durationInMillis) -> {

            final CompletableFuture<Boolean> booleanFuture = new CompletableFuture<>();

            // signal that future is completed (promise is resolved) after simulated delay
            nonBlockingSimulator.schedule(() -> booleanFuture.complete(true),
                    durationInMillis, TimeUnit.MILLISECONDS);
            return booleanFuture;
        };
        var task = new IoTask.Builder<>(UUID.randomUUID(), nonBlockingIoBehaviour)
                .duration(duration)
                .build();

        CompletableFuture<Boolean> waitForIt = ioBoundUseCase.run(task);
        waitForIt.thenAccept(success -> Util.sendIoResponse(routingCtx, success));
    }
}
