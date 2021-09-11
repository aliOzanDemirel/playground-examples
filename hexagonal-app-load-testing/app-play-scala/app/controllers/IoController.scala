package controllers

import akka.actor.ActorSystem
import business.entity.IoTask
import business.service.IoBoundUseCase
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{Json, Writes}
import play.api.mvc._

import java.util.UUID
import javax.inject._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Promise}

@Singleton
class IoController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem, ioBoundUseCase: IoBoundUseCase)
                            (implicit defaultAkkaDispatcher: ExecutionContext)
  extends AbstractController(cc) {

  private val log: Logger = LoggerFactory.getLogger(classOf[IoController])

  implicit val ioResponseSerializer: Writes[IoResponse] = Json.writes[IoResponse]

  // event loop thread pool will process all incoming requests, downstream processing is completely non-blocking
  def io(duration: Long): Action[AnyContent] = Action.async {

    simulateAsyncNonBlockingIo(duration)
      .future
      .map { result =>
        val jsonBody = Json.toJson(IoResponse(result))
        Ok(jsonBody)
      }(defaultAkkaDispatcher)
  }

  // implicit execution context should delegate to some other thread pool if downstream processing is blocking
  private def simulateAsyncNonBlockingIo(duration: Long): Promise[Boolean] = {

    log.debug("IO task with simulated non blocking (scheduled) io, duration: {}", duration)

    val promise: Promise[Boolean] = Promise[Boolean]()
    val behaviour: java.util.function.Function[java.lang.Long, Promise[Boolean]] = { duration: java.lang.Long =>

      // simulate non blocking task with scheduling promised value after some time
      val finiteDuration = FiniteDuration.apply(duration, MILLISECONDS)
      actorSystem.scheduler.scheduleOnce(finiteDuration) {

        promise.success(true)

      }(actorSystem.dispatcher)
      promise
    }

    val id = UUID.randomUUID()
    val task = new IoTask.IoTaskBuilder[Promise[Boolean]](id, behaviour)
      .duration(duration)
      .build()

    ioBoundUseCase.run(task)
  }
}
