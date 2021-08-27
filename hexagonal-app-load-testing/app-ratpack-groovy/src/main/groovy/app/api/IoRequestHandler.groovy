package app.api

import app.service.AppRatpackService
import com.google.inject.Inject
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.jackson.Jackson

class IoRequestHandler implements Handler {

    private AppRatpackService appRatpackService;

    @Inject
    IoRequestHandler(AppRatpackService appRatpackService) {
        this.appRatpackService = appRatpackService;
    }

    @Override
    void handle(Context ctx) throws Exception {

        def duration = ctx.request.queryParams.get('duration')
        long numberDuration = duration ? Long.parseLong(duration) : 1000

        appRatpackService
                .io(numberDuration)
                .then {
                    ctx.render(Jackson.json(success: it))
                }
    }
}
