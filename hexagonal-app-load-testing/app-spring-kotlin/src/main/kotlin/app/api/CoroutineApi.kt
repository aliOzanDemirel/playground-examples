package app.api

import app.service.AppSpringService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Profile("coroutine & !webmvc & !webflux")
@RestController
class CoroutineApi(private val appSpringService: AppSpringService) {

    /**
     * there is no need to return async data type (deferred result, future, publisher etc.) because of coroutine support of spring.
     * when handler methods are suspendable, spring launches coroutines to handle requests after accepting them from tomcat server.
     * spring by default configures 'Dispatchers.Unconfined' to let coroutine to be resumed by another thread.
     *
     * rest of the flow will work in non-blocking fashion for IO (suspendable call sites to schedule other coroutines within current
     * thread), downstream blocking call (delays coroutine, does not block carrier thread) will be scheduled to be resumed later, so
     * the carrier will be free to execute other coroutines.
     */

    @GetMapping("/io")
    suspend fun io(@RequestParam(required = false, defaultValue = "1000") duration: Long): BlockingResponse = withContext(Dispatchers.Unconfined) {

        return@withContext appSpringService.coroutineSuspendingIo(duration)
    }

    @PostMapping("/cpu")
    @ResponseStatus(HttpStatus.ACCEPTED)
    suspend fun cpu(@Valid @RequestBody body: WorkCpuRequest): WorkCpuResponse = withContext(Dispatchers.Unconfined) {

        return@withContext appSpringService.compute(body.inputs)
    }
}