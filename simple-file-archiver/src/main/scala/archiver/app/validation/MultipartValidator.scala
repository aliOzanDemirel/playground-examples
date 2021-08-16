package archiver.app.validation

import org.springframework.stereotype.Component
import org.springframework.validation.{Errors, Validator}
import org.springframework.web.multipart.MultipartFile

@Component
class MultipartValidator extends Validator {

  override def supports(clazz: Class[_]): Boolean = classOf[MultipartFile].isAssignableFrom(clazz)

  override def validate(target: Any, errors: Errors): Unit = {
    val file = target.asInstanceOf[MultipartFile]
    if (file != null && file.isEmpty) {
      errors.rejectValue("multipartFile", null,
        "You should select a file to create file record in DB.")
    }
  }

}
