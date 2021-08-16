package archiver.app.service

import archiver.app.common.Utils
import archiver.app.domain.Category
import archiver.app.repository.CategoryRepo
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConverters

@Service
class CategoryService @Autowired()(private val categoryRepo: CategoryRepo) {

  val log: Logger = LoggerFactory.getLogger(classOf[CategoryService])

  def getAllCategories: java.util.List[Category] = {
    categoryRepo.findAll().asInstanceOf[java.util.List[Category]]
  }

  def getCategoryById(id: Long): Category = {
    categoryRepo.findById(id).orElse(null)
  }

  def saveCategory(category: Category): Category = {
    categoryRepo.save(category)
  }

  def deleteCategoryAndRelatedFiles(categoryId: Long): Unit = {
    val category = getCategoryById(categoryId)

    val pathsToBeRemoved = JavaConverters.asScalaSet(category.files).map(_.savedPath)
    log.debug(s"Retrieved ${category.categoryName}, will remove its files: ${pathsToBeRemoved.toList}")
    pathsToBeRemoved.foreach(Utils.deleteFileByPath)

    categoryRepo.delete(category)
  }

}
