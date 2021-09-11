package app.dto;

public class IoResponse {

    public IoResponse(boolean success) {

        this.success = success;
    }

    private final boolean success;

    public boolean isSuccess() {
        return success;
    }
}
