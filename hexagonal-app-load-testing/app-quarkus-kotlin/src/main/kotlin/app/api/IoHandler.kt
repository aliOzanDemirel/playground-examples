package app.api

import app.config.AppExecutor
import business.entity.IoTask
import business.service.IoBoundUseCase
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.eclipse.microprofile.config.ConfigProvider
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import java.util.function.Function
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.validation.constraints.Min
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.QueryParam

@Path("/io")
@ApplicationScoped
class IoHandler {

    @Inject
    lateinit var ioBoundUseCase: IoBoundUseCase

    companion object {
        private val log = LoggerFactory.getLogger(IoHandler::class.java)

        // default is nonBlockingAsyncStreamBehaviour
        private var behaviour: Function<Long, Uni<Boolean>> = Function<Long, Uni<Boolean>> { durationInMillis ->
            Uni.createFrom().item(true)
                .onItem()
                .delayIt()
                .by(Duration.ofMillis(durationInMillis))
        }
    }

    @PostConstruct
    fun init() {

        val enableCoroutine = ConfigProvider.getConfig().getValue("enableCoroutine", Boolean::class.java)
        if (enableCoroutine) {

            // set IO action to nonBlockingCoroutineBehaviour
            behaviour = Function<Long, Uni<Boolean>> { durationInMillis ->
                GlobalScope.async {
                    delay(durationInMillis)
                    true
                }.asUni()
            }

            log.info("IO action will resolve to imperative non-blocking code that returns async publisher")
        } else {
            log.info("IO action will resolve to async reactive stream that delays without blocking")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @GET
    fun io(@QueryParam("duration") @DefaultValue("1000") @Min(1000) duration: Long): Uni<IoResponse> {

        return GlobalScope.async {
            coroutineSuspendingIo(duration, behaviour)
        }.asUni()
    }

    suspend fun coroutineSuspendingIo(duration: Long, behaviour: Function<Long, Uni<Boolean>>): IoResponse {

        return Uni.createFrom()
            .item {
                IoTask.IoTaskBuilder(UUID.randomUUID(), behaviour)
                    .duration(duration)
                    .build()
            }.flatMap { task ->
                log.debug("IO task with suspended coroutine, duration: {}", duration)
                ioBoundUseCase.run(task)
            }
            .onFailure()
            .recoverWithItem(false)
            .map {
                IoResponse(it)
            }
            // not really needed for this configuration since default executor is not event loop but a worker pool in resteasy classic
            .runSubscriptionOn(AppExecutor.io)
            .awaitSuspending()
    }
}