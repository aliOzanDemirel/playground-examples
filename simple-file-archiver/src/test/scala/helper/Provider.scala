package helper

import java.io.InputStream
import java.nio.file.attribute.PosixFilePermission
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import archiver.app.domain.{Category, File}
import org.springframework.http.HttpHeaders
import org.springframework.util.{LinkedMultiValueMap, MultiValueMap}

object Provider {

  final val totalCategories = 3
  final val totalFiles = 10
  final val categoryNamePattern = "Category-{index}"
  final val fileNamePattern = "File-{index}"

  def getTestFileStream(resourcePath: String): InputStream = {
    getClass.getResourceAsStream(resourcePath)
  }

  def buildName(name: String, index: Int): String = {
    name.replace("{index}", String.valueOf(index))
  }

  def loadCategories[T](whatToDo: Category => T): Unit = {
    for (i <- 1 to totalCategories) {
      var cat = new Category
      cat.setId(i)
      cat.setCategoryName(buildName(categoryNamePattern, i))
      cat.setCategoryDescription("CategoryDesc-" + i)
      cat.setApprovalDate(LocalDate.now().minusMonths(i))
      whatToDo(cat)
    }
  }

  def loadFiles[T](whatToDo: File => T, categoryFinder: Int => Category): Unit = {
    for (i <- 1 to totalFiles) {
      var file = new File
      file.setId(i)
      file.setFileName(buildName(fileNamePattern, i))
      file.setCategory(categoryFinder(i))
      file.setFilePermissions(PosixFilePermission.values()(i % 9).name())
      file.setSavedPath("/test/path/" + i)
      file.setIsRemoved(true)
      whatToDo(file)
    }
  }

  def getSingleCategory(catName: String): Category = {
    val category = new Category
    category.id = 123
    category.categoryName = catName
    category.categoryDescription = "Some Desc"
    category.approvalDate = LocalDate.now()
    category
  }

  def getCategoryFormData(cat: Category): MultiValueMap[String, Object] = {
    val formBody = new LinkedMultiValueMap[String, Object]
    formBody.add("categoryName", cat.categoryName)
    formBody.add("categoryDescription", cat.categoryDescription)
    formBody.add("approvalDate", cat.approvalDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
    formBody
  }

  def getRequestHeaders(accept: String, contentType: String): HttpHeaders = {
    val headers = new HttpHeaders
    headers.add(HttpHeaders.ACCEPT, accept)
    headers.add(HttpHeaders.CONTENT_TYPE, contentType)
    headers
  }

}
