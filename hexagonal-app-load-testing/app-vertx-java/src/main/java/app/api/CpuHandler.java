package app.api;

import app.dto.WorkCpuRequest;
import app.dto.WorkCpuResponse;
import business.entity.CpuTask;
import business.service.CpuBoundUseCase;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class CpuHandler implements Function<RoutingContext, Future<WorkCpuResponse>> {

    private static final Logger log = LoggerFactory.getLogger(CpuHandler.class);

    private final CpuBoundUseCase cpuBoundUseCase;

    public CpuHandler(CpuBoundUseCase cpuBoundUseCase) {
        this.cpuBoundUseCase = cpuBoundUseCase;
    }

    @Override
    public Future<WorkCpuResponse> apply(RoutingContext routingContext) {

        var request = routingContext.getBodyAsJson().mapTo(WorkCpuRequest.class);
        if (request == null || request.getInputs() == null || request.getInputs().isEmpty()) {

            routingContext.response()
                    .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .end("CPU work inputs cannot be empty!");
            return Future.succeededFuture();
        }

        log.debug("CPU task with {} inputs", request.getInputs().size());
        var task = new CpuTask.Builder(request.getInputs()).build();
        var responses = cpuBoundUseCase.compute(task);
        long durationInNanos = responses.stream().mapToLong(it -> it.inputProcessingDuration).sum();

        routingContext.response().setStatusCode(HttpResponseStatus.ACCEPTED.code());
        return Future.succeededFuture(new WorkCpuResponse(durationInNanos));
    }
}
