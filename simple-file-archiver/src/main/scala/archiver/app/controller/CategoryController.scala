package archiver.app.controller

import archiver.app.common.Utils.errorMap
import archiver.app.common.{Mappings, Utils}
import archiver.app.domain.Category
import archiver.app.service.CategoryService
import javax.validation.Valid
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, MediaType, ResponseEntity}
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class CategoryController @Autowired()(private val categoryService: CategoryService) {

  val log: Logger = LoggerFactory.getLogger(classOf[CategoryController])

  @GetMapping(Array(Mappings.ALL_CATEGORIES))
  def listCategories(model: Model): String = {
    model.addAttribute("categories", categoryService.getAllCategories)

    val category =
      if (model.containsAttribute("category"))
        model.asMap().get("category")
      else
        new Category
    model.addAttribute("category", category)

    Utils.decorateMainPage(model, "category/list-categories", "list-categories")
  }

  @PostMapping(value = Array(Mappings.SAVE_CATEGORY))
  def saveCategoryFromForm(@Valid @ModelAttribute(name = "category") category: Category,
                           binding: BindingResult, redirect: RedirectAttributes): String = {
    log.debug(s"Category with name ${category.categoryName} is being saved")

    if (binding.hasErrors) {
      redirect.addFlashAttribute("org.springframework.validation.BindingResult.category", binding)
      redirect.addFlashAttribute("category", category)
    } else {
      categoryService.saveCategory(category)
    }

    Mappings.REDIRECT_TO_CATEGORIES
  }

  @PostMapping(value = Array(Mappings.DELETE_CATEGORY), produces = Array(MediaType.APPLICATION_JSON_VALUE))
  @ResponseBody
  def deleteCategoryAndFiles(@RequestParam(name = "categoryId", required = true) categoryId: Long):
  ResponseEntity[Map[String, Object]] = {
    log.debug(s"Request to ${Mappings.DELETE_CATEGORY} to remove record with ID: $categoryId")

    if (categoryId == 0) {
      ResponseEntity.badRequest()
        .body(Map("errorMessage" -> "ID of file is not found in request body!"))
        .asInstanceOf[ResponseEntity[Map[String, Object]]]
    } else {
      try {
        categoryService.deleteCategoryAndRelatedFiles(categoryId)
        ResponseEntity.ok(null)
      } catch {
        case ex: Exception =>
          log.error(s"Error occured while deleting category with ID: $categoryId", ex)
          ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap(ex.getMessage))
      }
    }
  }

}
