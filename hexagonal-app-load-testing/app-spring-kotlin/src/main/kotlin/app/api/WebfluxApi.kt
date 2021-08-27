package app.api

import app.service.AppSpringService
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import javax.validation.Valid

@Profile("!coroutine & !webmvc & webflux")
@RestController
class WebfluxApi(private val appSpringService: AppSpringService) {

    /**
     * reactive publishers are returned for spring to subscribe to stream when generating response.
     * io task will be resolved with fully reactive non-blocking stream thanks to mono delay not blocking the processing thread.
     */

    @GetMapping("/io")
    fun io(@RequestParam(required = false, defaultValue = "1000") duration: Long): Mono<BlockingResponse> {

        return appSpringService.reactorDelayingIo(duration)
    }

    @PostMapping("/cpu")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun cpu(@Valid @RequestBody body: Mono<WorkCpuRequest>): Mono<WorkCpuResponse> {

        return body.map {
            appSpringService.compute(it.inputs)
        }
    }
}