package app.api.error

import io.netty.handler.codec.http.HttpResponseStatus
import ratpack.http.ClientErrorException

class BadRequest extends ClientErrorException {

    BadRequest(String message) {
        super(message)
    }

    @Override
    int getClientErrorCode() {
        return HttpResponseStatus.BAD_REQUEST.code()
    }
}
