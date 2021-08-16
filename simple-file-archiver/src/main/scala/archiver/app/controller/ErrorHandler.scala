package archiver.app.controller

import archiver.app.common.Utils
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ErrorHandler extends ErrorController {

  val log: Logger = LoggerFactory.getLogger(classOf[ErrorHandler])

  override def getErrorPath = "/error"

  @GetMapping(Array("/error"))
  def handleError(request: HttpServletRequest, model: Model): String = {

    val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)
    val errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE)
    val exceptionType = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE)
    var requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)
    var error = "Error occured."

    if (status != null) {
      error += " Status: " + status
    }
    if (errorMessage != null) {
      error += " Message: " + errorMessage
    }
    if (exceptionType != null) {
      error += " Exception Type: " + exceptionType
    }
    if (requestUri != null) {
      error += " Requested: " + requestUri
    }

    log.error(error)
    model.addAttribute("errorMessage", error)

    Utils.decorateMainPage(model, "common/alert",
      "alert (type='danger', message=${errorMessage})")
  }

}
