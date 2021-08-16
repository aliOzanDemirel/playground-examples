package integration

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import archiver.app.AppConf
import archiver.app.common.Mappings
import archiver.app.repository.CategoryRepo
import archiver.app.service.FileService
import helper.Provider.{getCategoryFormData, getRequestHeaders, getSingleCategory}
import org.junit.runner.RunWith
import org.junit.{Assert, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.MediaType.{APPLICATION_FORM_URLENCODED_VALUE, APPLICATION_JSON_VALUE, TEXT_HTML_VALUE}
import org.springframework.http._
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.{LinkedMultiValueMap, StringUtils}

@RunWith(value = classOf[SpringRunner])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = Array(classOf[AppConf], classOf[IntegrationTestConfig]))
@TestPropertySource(locations = Array("classpath:integration-test.properties"))
class ArchiverTestRunner {

  @Autowired
  private val fileService: FileService = null

  @Autowired
  private val categoryRepo: CategoryRepo = null

  @Autowired
  private val template: TestRestTemplate = null

  @Test
  def saveCategory(): Unit = {

    val cat = getCategoryFormData(getSingleCategory("integration-test"))
    val requestEntity = new HttpEntity(cat, getRequestHeaders(TEXT_HTML_VALUE, APPLICATION_FORM_URLENCODED_VALUE))

    val resp = template.exchange(Mappings.SAVE_CATEGORY, HttpMethod.POST, requestEntity, classOf[String])

    Assert.assertTrue("POSTing category should return FOUND (302)!",
      resp.getStatusCode == HttpStatus.FOUND)

    Assert.assertTrue("POSTing category should redirect to /categories page!",
      StringUtils.endsWithIgnoreCase(resp.getHeaders.get(HttpHeaders.LOCATION).get(0), "/categories"))

    // retrieve newly posted category to check if it is saved
    val newlyAddedCat = categoryRepo.findByCategoryName(cat.getFirst("categoryName").asInstanceOf[String]).get(0)

    Assert.assertEquals("Newly created category's description does not match!",
      newlyAddedCat.getCategoryDescription, cat.getFirst("categoryDescription").asInstanceOf[String])

    Assert.assertEquals("Newly created category's approval date does not match!",
      newlyAddedCat.getApprovalDate, LocalDate.parse(cat.getFirst("approvalDate")
        .asInstanceOf[String], DateTimeFormatter.ISO_LOCAL_DATE))
  }

  @Test
  def deleteCategorySuccessfully(): Unit = {

    val idToRemove = 3.toLong
    val filesToBeRemoved = categoryRepo.findById(idToRemove).get().files

    val bodyMap = new LinkedMultiValueMap[String, String]() {
      add("categoryId", idToRemove.toString)
    }
    val requestEntity = new HttpEntity(bodyMap,
      getRequestHeaders(APPLICATION_JSON_VALUE, APPLICATION_FORM_URLENCODED_VALUE))

    val resp = template.exchange(Mappings.DELETE_CATEGORY, HttpMethod.POST, requestEntity, classOf[String])

    Assert.assertTrue("POSTing to delete category should return OK (200)!",
      resp.getStatusCode == HttpStatus.OK)

    val categoryThatShouldntExist = categoryRepo.findById(idToRemove).orElse(null)

    Assert.assertNull("Category should have been removed!", categoryThatShouldntExist)

    filesToBeRemoved.forEach(file => Assert.assertNull(
      s"File ${file.fileName} in removed category should have been removed!", fileService.getFileById(file.id)))
  }

}
