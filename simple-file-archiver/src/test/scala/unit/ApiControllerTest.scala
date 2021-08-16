package unit

import java.util

import archiver.app.AppConf
import archiver.app.controller.ApiController
import archiver.app.domain.{Category, File}
import archiver.app.service.{CategoryService, FileService}
import helper.Provider
import helper.Provider._
import org.junit.runner.RunWith
import org.junit.{Before, Test}
import org.mockito.BDDMockito._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders._
import org.springframework.test.web.servlet.result.MockMvcResultMatchers._

@RunWith(value = classOf[SpringRunner])
@WebMvcTest(controllers = Array(classOf[ApiController]), secure = false)
@ContextConfiguration(classes = Array(classOf[AppConf]))
class ApiControllerTest {

  @Autowired
  var mvc: MockMvc = _

  @MockBean
  var categoryService: CategoryService = _

  @MockBean
  var fileService: FileService = _

//  @Autowired
  //  var apiController: ApiController = _

  @Before
  def before(): Unit = {

    val categories: util.List[Category] = new util.ArrayList()
    Provider.loadCategories(cat => categories.add(cat))

    val files: util.List[File] = new util.ArrayList()
    Provider.loadFiles(files.add, categoryId => categories.get(categoryId % 3))

    given(categoryService.getAllCategories).willReturn(categories)
    given(fileService.getAllFiles).willReturn(files)
  }

  @Test
  def testGetCategoriesInJSON(): Unit = {

    mvc.perform(get("/api/categories"))
      .andExpect(status.isOk)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$.length()").value(totalCategories))
      .andExpect(jsonPath("$.[0].categoryName").value(buildName(categoryNamePattern, 1)))
      .andExpect(jsonPath("$..categoryDescription").exists())
      .andExpect(jsonPath("$.[1].approvalDate").isString)
      .andExpect(jsonPath("$..files").doesNotExist())
  }

  @Test
  def testFilesInJSON(): Unit = {

    mvc.perform(get("/api/files"))
      .andExpect(status.isOk)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$.length()").value(totalFiles))
      .andExpect(jsonPath("$.[2].fileName").value(buildName(fileNamePattern, 3)))
      .andExpect(jsonPath("$..savedPath").exists())
      .andExpect(jsonPath("$..filePermissions").isNotEmpty)
      .andExpect(jsonPath("$.[1].isRemoved").isBoolean)
      .andExpect(jsonPath("$.[0].category").isMap)
  }

}
