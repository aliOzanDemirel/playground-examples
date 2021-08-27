package app.api

import app.service.AppSpringService
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Profile("!coroutine & webmvc & !webflux")
@RestController
class WebmvcApi(private val appSpringService: AppSpringService) {

    /**
     * these handler methods can return async type (CompletableFuture etc.) for spring to register them with async dispatch to delegate
     * processing to another thread to release tomcat worker thread. ideally there should also be async executor pool configured to
     * delegate work (like tomcat worker pool), using common fork join pool does not make sense for blocking/long running operations.
     *
     * alternatively these methods can be made suspendable so that incoming requests are dispatched by launching coroutines.
     */

    @GetMapping("/io")
    fun io(@RequestParam(required = false, defaultValue = "1000") duration: Long): BlockingResponse {

        return appSpringService.threadBlockingIo(duration)
    }

    @PostMapping("/cpu")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun cpu(@Valid @RequestBody body: WorkCpuRequest): WorkCpuResponse {

        return appSpringService.compute(body.inputs)
    }
}