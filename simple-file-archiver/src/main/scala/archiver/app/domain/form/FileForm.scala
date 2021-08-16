package archiver.app.domain.form

import java.nio.file.attribute.PosixFilePermission

import javax.validation.constraints._
import org.springframework.web.multipart.MultipartFile

import scala.beans.BeanProperty

class FileForm {

  @BeanProperty
  var id: java.lang.Long = _

  @Size(min = 2, max = 40, message = "File name size should be between {min} and {max} characters.")
  @BeanProperty
  var fileName: String = _

  @BeanProperty
  var filePath: String = _

  @NotEmpty(message = "At least one permission should be assigned.")
  @BeanProperty
  var selectedFilePermissions: Array[PosixFilePermission] = _

  @NotNull(message = "Valid category should be selected.")
  @Positive(message = "Valid category should be selected.")
  @BeanProperty
  var categoryId: java.lang.Long = _

  @BeanProperty
  var multipartFile: MultipartFile = _

}
