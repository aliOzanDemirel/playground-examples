package app;

import app.api.BlockingIoHandler;
import app.api.CpuHandler;
import app.api.NonBlockingIoHandler;
import app.util.DependencyFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(AppVerticle.class);

    // initializer when verticle is deployed
    @Override
    public void start() {

        configureAndStartVertx(vertx);
    }

    public static void configureAndStartVertx(Vertx vertx) {

        var router = Router.router(vertx);

        // 2 MB request body limit
        var bodyHandler = BodyHandler.create().setBodyLimit(2 * 1024 * 1024);
        router.route().handler(bodyHandler);

        if (Main.isBlockingIoEnabled()) {

            var ioHandler = new BlockingIoHandler(DependencyFactory.ioBoundUseCase());
            router.get("/io").blockingHandler(ioHandler, false);
            log.info("Configured blocking IO...");
        } else {

            var ioHandler = new NonBlockingIoHandler(DependencyFactory.ioBoundUseCase());
            router.get("/io").handler(ioHandler);
            log.info("Configured non-blocking IO...");
        }

        // technically long running cpu work is also blocking, so such processing can
        // also be delegated to workers to not block event loop threads
        var cpuHandler = new CpuHandler(DependencyFactory.cpuBoundUseCase());
        router.post("/cpu").respond(cpuHandler);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080);

        log.info("Started server, isWorkerContext: {}, isEventLoopContext: {}",
                vertx.getOrCreateContext().isWorkerContext(),
                vertx.getOrCreateContext().isEventLoopContext());
    }

}