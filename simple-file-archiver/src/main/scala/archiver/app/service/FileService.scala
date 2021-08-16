package archiver.app.service

import java.io._
import java.lang.Long
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{Files, Paths}

import archiver.app.common.{ServiceResp, Utils}
import archiver.app.domain.form.FileForm
import archiver.app.domain.{Category, File}
import archiver.app.repository.FileRepo
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

import scala.collection.JavaConverters

@Service
class FileService @Autowired()(private val fileRepo: FileRepo,
                               @Value("${app.file.path}") var rootFilePath: String) {

  val log: Logger = LoggerFactory.getLogger(classOf[FileService])

  def getAllFiles: java.util.List[File] = {
    fileRepo.findAll().asInstanceOf[java.util.List[File]]
  }

  def getFileById(id: Long): File = {
    fileRepo.findById(id).orElse(null)
  }

  def saveFile(fileForm: FileForm, category: Category): Option[File] = {

    var file = new File
    try {
      file.setId(fileForm.id)
      file.setFileName(fileForm.fileName)
      file.setFilePermissions(Utils.permissionsToString(fileForm.selectedFilePermissions.toList))
      file.setCategory(category)

      val multipartFile = fileForm.multipartFile
      if (multipartFile != null) {
        log.debug(s"Multipart file is being saved, original file name: ${multipartFile.getOriginalFilename}")

        file.setIsRemoved(false)
        file.setSavedPath(pathToFile(category.categoryName, multipartFile.getOriginalFilename))
        uploadFile(multipartFile, file.savedPath, fileForm.selectedFilePermissions)
      }

      return Some(fileRepo.save(file))
    } catch {
      case ex: IOException =>
        log.error(s"Error while writing file ${fileForm.fileName} to ${file.savedPath}", ex)
      case ex: Exception =>
        log.error(s"Error while saving file ${fileForm.fileName} with category ${category.categoryName}", ex)
    }
    None
  }

  def uploadFile(multipartFile: MultipartFile, fullPath: String, permissions: Array[PosixFilePermission]): Unit = {

    val ioFilePath = Paths.get(fullPath)
    val parent = ioFilePath.getParent

    if (Files.notExists(parent)) {
      Files.createDirectories(parent)
    }

    val bos = new BufferedOutputStream(new FileOutputStream(ioFilePath.toFile))
    try
      bos.write(multipartFile.getBytes)
    finally
      bos.close()

    Files.setPosixFilePermissions(ioFilePath, JavaConverters.setAsJavaSet(permissions.toSet))
  }

  def pathToFile(subFolderName: String, persistedFileName: String): String = {
    val sep = java.io.File.separator
    rootFilePath + sep + subFolderName + sep + persistedFileName
  }

  def deleteFile(fileId: Long): ServiceResp = {
    applyFunctionWithRetrievedFile(fileId,
      file => {
        file.setIsRemoved(true)
        fileRepo.save(file)
        Utils.deleteFileByPath(pathStr = file.savedPath)
      })
  }

  def readFileToBytes(fileId: Long): ServiceResp = {
    applyFunctionWithRetrievedFile(fileId,
      file => {
        Files.readAllBytes(Paths.get(file.savedPath))
      })
  }

  def applyFunctionWithRetrievedFile(fileId: Long, f: File => Object): ServiceResp = {
    val resp = new ServiceResp
    var msg = "File with ID: " + fileId

    val file = getFileById(fileId)
    if (file == null) {
      resp.status = HttpStatus.NOT_FOUND
      resp.errorMessage = msg + " cannot be found"
      log.warn(resp.errorMessage)
    } else if (file.isRemoved) {
      resp.status = HttpStatus.GONE
      resp.errorMessage = msg + " is removed before"
      log.info(resp.errorMessage)
    } else {
      resp.result = f(file)
    }
    resp
  }

}
