package archiver.app.repository

import archiver.app.domain.{Category, File}
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
trait CategoryRepo extends CrudRepository[Category, java.lang.Long] {

  def findByCategoryName(categoryName: String): java.util.List[Category]

}

@Repository
trait FileRepo extends CrudRepository[File, java.lang.Long] {}
