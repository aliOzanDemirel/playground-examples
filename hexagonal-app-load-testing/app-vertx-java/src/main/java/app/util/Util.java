package app.util;

import app.dto.IoResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class Util {

    private static final String JSON_CONTENT_TYPE = "application/json";

    private Util() {
    }

    public static long readDurationQueryParam(RoutingContext routingCtx) {

        String durationParam = routingCtx.request().getParam("duration");
        if (durationParam == null) {
            durationParam = "1000";
        }

        try {
            return Long.parseLong(durationParam);
        } catch (NumberFormatException e) {

            routingCtx.response()
                    .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .end("Duration should be positive number!");
            return -1;
        }
    }

    public static void sendIoResponse(RoutingContext routingCtx, boolean success) {

        routingCtx.response()
                .putHeader("content-type", JSON_CONTENT_TYPE)
                .end(Json.encodePrettily(new IoResponse(success)));
    }
}
