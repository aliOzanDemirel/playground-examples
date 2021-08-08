package app.config.security.jwt;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus (value = HttpStatus.UNAUTHORIZED)
public class InvalidJwtAuthenticationException extends AuthenticationException {

    public InvalidJwtAuthenticationException(String e) {
        super(e);
    }
}