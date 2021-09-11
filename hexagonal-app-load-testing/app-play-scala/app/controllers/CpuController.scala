package controllers

import akka.actor.ActorSystem
import business.service.CpuBoundUseCase
import business.service.dto.WorkCpuCommand
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsObject, JsValue, Json, Reads, Writes}
import play.api.mvc._

import java.util
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CpuController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem, cpuBoundUseCase: CpuBoundUseCase)
  extends AbstractController(cc) {

  // delegate to separate thread pool since cpu work will keep the threads busy
  implicit val computeExecutor: ExecutionContext = actorSystem.dispatchers.lookup("ctx.compute-pool")

  private val log: Logger = LoggerFactory.getLogger(classOf[CpuController])

  // request body exactly matches case class description
  implicit val workCpuRequestDeserializer: Reads[WorkCpuRequest] = Json.reads[WorkCpuRequest]

  // for play to apply when serializing custom type, normally needed for custom mappings, not 1 to 1 mapping from dto
  implicit val workCpuResponseSerializer: Writes[WorkCpuResponse] = new Writes[WorkCpuResponse] {
    override def writes(response: WorkCpuResponse): JsObject = {
      Json.obj(
        "nanoseconds" -> response.nanoseconds,
        "milliseconds" -> response.milliseconds,
        "seconds" -> response.seconds
      )
    }
  }

  // 2 MB maximum body limit
  def cpu: Action[JsValue] = Action(parse.json(2_097_152)).async { implicit request =>

    Future {
      var result: Result = null

      val workCpuRequest = request.body.as[WorkCpuRequest]
      if (workCpuRequest == null || workCpuRequest.inputs == null || workCpuRequest.inputs.isEmpty) {

        result = BadRequest("CPU work inputs cannot be empty!")

      } else {

        // conversion to java list instead of providing custom serializer...
        val javaList: java.util.List[String] = new util.ArrayList[String](workCpuRequest.inputs.size)
        workCpuRequest.inputs.foreach(javaList.add)

        val response = compute(javaList)
        result = Accepted(Json.toJson(response))
      }
      result
    }(executor = computeExecutor)
  }

  def compute(inputs: java.util.List[String]): WorkCpuResponse = {

    log.debug("CPU task with {} inputs", inputs.size)

    val command = new WorkCpuCommand(inputs)
    val durationInNanos = cpuBoundUseCase.workCpu(command)

    if (durationInNanos != null) {

      val milliseconds = durationInNanos / 1_000_000
      val seconds = (milliseconds / 1_000).toInt
      return WorkCpuResponse(Option(durationInNanos), Option(milliseconds), Option(seconds))
    }

    // play serializes absent value to null
    WorkCpuResponse(Option.empty[Long], Option.empty[Long], Option.empty[Int])
  }
}