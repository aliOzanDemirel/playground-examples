package transaction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NO_CONTENT)
public class OldTransactionException extends RuntimeException {

    public OldTransactionException(String message) {
        super(message);
    }

}
