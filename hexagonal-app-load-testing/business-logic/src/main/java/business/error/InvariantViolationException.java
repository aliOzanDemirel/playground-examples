package business.error;

public class InvariantViolationException extends RuntimeException {

    public InvariantViolationException(String errorMessage) {
        super(errorMessage);
    }
}
