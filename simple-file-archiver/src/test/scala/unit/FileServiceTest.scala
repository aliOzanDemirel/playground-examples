package unit

import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{Files, Paths}
import java.util.Optional

import archiver.app.domain.Category
import archiver.app.domain.form.FileForm
import archiver.app.repository.FileRepo
import archiver.app.service.FileService
import helper.Provider
import helper.Provider.getTestFileStream
import org.junit.{Before, Test}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito._
import org.springframework.mock.web.MockMultipartFile
import org.springframework.util.FileCopyUtils._

class FileServiceTest {

  val categoryName = "download"
  val testFileName: String = "test-file.txt"
  val uploadedFileName: String = "uploaded-test-file.txt"
  val testFilePath = s"/$categoryName/$testFileName"
  val uploadedFilePath = s"/$categoryName/$uploadedFileName"

  @Mock
  var fileRepo: FileRepo = _

  @InjectMocks
  var fileService: FileService = _

  @Before
  def before(): Unit = {
    MockitoAnnotations.initMocks(this)
    val resFullPath = getClass.getResource(testFilePath).getPath
    val resourcePath = resFullPath.substring(0, resFullPath.lastIndexOf(testFilePath))
    fileService.rootFilePath = resourcePath

    // return the file object that is saved to db
    when(fileRepo.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg())
  }

  def getFileForm(cat: Category): FileForm = {
    val file = new FileForm
    file.id = Long.box(1234)
    file.fileName = "test-file.txt"
    file.categoryId = Long.box(cat.id)
    file.filePath = fileService.pathToFile(cat.categoryName, file.fileName)
    file.selectedFilePermissions = Array(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
    file.multipartFile = new MockMultipartFile(uploadedFileName, uploadedFileName,
      null, getTestFileStream(testFilePath))
    file
  }

  @Test
  def testSaveAndDeleteFile(): Unit = {

    // save a file record
    val category = Provider.getSingleCategory(categoryName)
    val fileForm = getFileForm(category)
    val fileOption = fileService.saveFile(fileForm, category)

    assert(fileOption.isDefined)
    verify(fileRepo, times(1)).save(any())

    val savedFile = fileOption.get
    assert(savedFile != null)
    assert(savedFile.getCategory.categoryName == category.categoryName)
    assert(copyToByteArray(getTestFileStream(testFilePath))
      sameElements copyToByteArray(getTestFileStream(uploadedFilePath)))

    // delete the file record with its persisted text file
    when(fileRepo.findById(fileForm.id)).thenReturn(Optional.ofNullable(savedFile))

    val resp = fileService.deleteFile(fileForm.id)
    assert(resp.result != null)
    assert(resp.result.isInstanceOf[String])
    assert(!Files.exists(Paths.get(savedFile.savedPath)))

    verify(fileRepo, atLeastOnce()).findById(fileForm.id)
  }

}
