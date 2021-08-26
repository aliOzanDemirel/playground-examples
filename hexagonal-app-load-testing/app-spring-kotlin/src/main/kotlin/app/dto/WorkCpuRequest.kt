package app.dto

import javax.validation.constraints.NotEmpty

class WorkCpuRequest {

    @NotEmpty
    val inputs: List<String> = ArrayList()
}