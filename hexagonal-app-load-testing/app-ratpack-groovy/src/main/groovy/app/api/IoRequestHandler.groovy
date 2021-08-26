package app.api

import app.service.BusinessLogicClient
import com.google.inject.Inject
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.jackson.Jackson

class IoRequestHandler implements Handler {

    private BusinessLogicClient businessLogicClient;

    @Inject
    IoRequestHandler(BusinessLogicClient businessLogicClient) {
        this.businessLogicClient = businessLogicClient;
    }

    @Override
    void handle(Context ctx) throws Exception {

        def duration = ctx.request.queryParams.get('duration')
        int numberDuration = duration ? Integer.parseInt(duration) : 1000

        businessLogicClient
                .blockingIo(numberDuration)
                .then {
                    ctx.render(Jackson.json(success: it))
                }
    }
}
