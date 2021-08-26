package app.dto

import app.api.error.BadRequest
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class WorkCpuRequest {

    List<String> inputs

    def internalStateValidation = { ->
        if (!inputs) {
            throw new BadRequest("CPU work inputs cannot be empty!")
        }
        this
    }
}