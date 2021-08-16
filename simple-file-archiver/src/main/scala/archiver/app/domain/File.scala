package archiver.app.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import javax.persistence._
import javax.validation.constraints.{NotBlank, NotNull, Size}

import scala.beans.BeanProperty

@Entity
@Table(name = "files")
@JsonIgnoreProperties(ignoreUnknown = true, value = Array("hibernateLazyInitializer", "handler"))
class File {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @BeanProperty
  var id: Long = _

  @Size(min = 2, max = 40, message = "File name size should be between {min} and {max} characters.")
  @BeanProperty
  var fileName: String = _

  @NotBlank
  @BeanProperty
  var savedPath: String = _

  // removed means the file does not exist where savedPath points
  @NotNull
  @BeanProperty
  var isRemoved: Boolean = false

  @NotBlank
  @BeanProperty
  var filePermissions: String = _

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "fk_category_id", nullable = false)
  @NotNull
  @BeanProperty
  var category: Category = _

}
