package app.api

import app.api.error.BadRequest
import app.dto.WorkCpuRequest
import app.service.AppRatpackService
import com.google.inject.Inject
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler
import ratpack.http.Status
import ratpack.jackson.Jackson
import ratpack.parse.ParseException

@Slf4j
class CpuRequestHandler extends GroovyHandler {

    private AppRatpackService appRatpackService;

    @Inject
    CpuRequestHandler(AppRatpackService appRatpackService) {
        this.appRatpackService = appRatpackService;
    }

    @Override
    protected void handle(GroovyContext context) {

        context.parse(Jackson.fromJson(WorkCpuRequest.class))
                .onError {
                    if (it instanceof ParseException) {
                        log.error("Could not parse request body!", it)
                        throw new BadRequest("Invalid request body!")
                    }
                }
                .map { request -> request.internalStateValidation.call() }
                .flatMap { request -> appRatpackService.compute(request.inputs) }
                .then { response ->
                    context.response.status(Status.ACCEPTED)
                    context.render Jackson.json(response)
                }
    }
}
