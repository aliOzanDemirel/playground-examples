package controllers

case class WorkCpuRequest(inputs: List[String] = List())

case class IoResponse(success: Boolean)

case class WorkCpuResponse(nanoseconds: Option[Long], milliseconds: Option[Long], seconds: Option[Int])