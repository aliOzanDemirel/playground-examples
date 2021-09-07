package app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BlockingResponse {

    public BlockingResponse(boolean success) {

        this.success = success;
    }

    private final boolean success;

    @JsonProperty
    public boolean isSuccess() {
        return success;
    }
}
