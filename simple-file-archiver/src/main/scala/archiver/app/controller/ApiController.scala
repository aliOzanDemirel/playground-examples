package archiver.app.controller

import archiver.app.common.Mappings
import archiver.app.domain.{Category, File}
import archiver.app.service.{CategoryService, FileService}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._

import scala.collection.JavaConverters

@RestController
class ApiController @Autowired()(private val fileService: FileService,
                                 private val categoryService: CategoryService) {

  val log: Logger = LoggerFactory.getLogger(classOf[ApiController])

  @GetMapping(Array(Mappings.API_FILES))
  def getFiles: List[File] = {
    log.info("Request to " + Mappings.API_FILES)

    JavaConverters.asScalaIterator(fileService.getAllFiles.iterator()).toList
  }

  @GetMapping(Array(Mappings.API_CATEGORIES))
  def getCategories: List[Category] = {
    log.info("Request to " + Mappings.API_CATEGORIES)

    JavaConverters.asScalaIterator(categoryService.getAllCategories.iterator()).toList
  }

}
