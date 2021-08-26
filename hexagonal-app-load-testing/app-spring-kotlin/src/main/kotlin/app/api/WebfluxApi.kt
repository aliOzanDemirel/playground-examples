package app.api

import app.dto.BlockingResponse
import app.dto.WorkCpuRequest
import app.dto.WorkCpuResponse
import app.service.BusinessLogicClient
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import javax.validation.Valid

@Profile("!coroutine & !webmvc & webflux")
@RestController
class WebfluxApi(private val businessLogicClient: BusinessLogicClient) {

    /**
     * reactive publishers are returned for spring to subscribe to stream when generating response.
     */

    @GetMapping("/io")
    fun io(@RequestParam(required = false, defaultValue = "1000") duration: Int): Mono<BlockingResponse> {

        return Mono.defer {
            Mono.just(businessLogicClient.blockingIo(duration))
        }
    }

    @PostMapping("/cpu")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun cpu(@Valid @RequestBody body: Mono<WorkCpuRequest>): Mono<WorkCpuResponse> {

        return body.map {
            businessLogicClient.workCpu(it.inputs)
        }
    }
}