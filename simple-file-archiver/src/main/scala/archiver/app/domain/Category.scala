package archiver.app.domain

import java.time.LocalDate

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonIgnoreProperties}
import javax.persistence._
import javax.validation.constraints.{PastOrPresent, Size}
import org.springframework.format.annotation.DateTimeFormat

import scala.beans.BeanProperty

@Entity
@Table(name = "categories")
@JsonIgnoreProperties(ignoreUnknown = true, value = Array("hibernateLazyInitializer", "handler"))
class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @BeanProperty
  var id: Long = _

  @Size(min = 2, max = 40, message = "Name should be between {min} and {max} characters.")
  @BeanProperty
  var categoryName: String = _

  @Size(max = 255, message = "Description should not exceed {max} characters.")
  @BeanProperty
  var categoryDescription: String = _

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @PastOrPresent(message = "Approval date cannot be in the future.")
  @BeanProperty
  var approvalDate: LocalDate = _

  @JsonIgnore
  @OneToMany(mappedBy = "category", cascade = Array(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.EAGER)
  var files: java.util.Set[File] = new java.util.HashSet[File]

}
