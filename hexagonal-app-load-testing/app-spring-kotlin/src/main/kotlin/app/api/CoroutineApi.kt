package app.api

import app.dto.BlockingResponse
import app.dto.WorkCpuRequest
import app.dto.WorkCpuResponse
import app.service.BusinessLogicClient
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Profile("coroutine & !webmvc & !webflux")
@RestController
class CoroutineApi(private val businessLogicClient: BusinessLogicClient) {

    /**
     * there is no need to return async data type (deferred result, future, publisher etc.) because of coroutine support of spring.
     * when handler methods are suspendable, spring launches coroutines to handle requests after accepting them from tomcat server.
     *
     * ideally, rest of the flow should work in non-blocking fashion (suspendable call sites to schedule other coroutines within current
     * thread). request processing flow is not really reactive since downstream call blocks the underlying thread for simulated io
     * (blocks the carrier thread, it does not 'delay' coroutine), this is just demonstration of wiring coroutine with spring webflux.
     */

    @GetMapping("/io")
    suspend fun io(@RequestParam(required = false, defaultValue = "1000") duration: Int): BlockingResponse {

        return businessLogicClient.blockingIo(duration)
    }

    @PostMapping("/cpu")
    @ResponseStatus(HttpStatus.ACCEPTED)
    suspend fun cpu(@Valid @RequestBody body: WorkCpuRequest): WorkCpuResponse {

        return businessLogicClient.workCpu(body.inputs)
    }
}