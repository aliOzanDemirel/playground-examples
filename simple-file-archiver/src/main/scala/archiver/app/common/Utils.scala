package archiver.app.common

import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{Files, Paths}

import archiver.app.domain.File
import archiver.app.domain.form.FileForm
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.ui.Model

class ServiceResp(var status: HttpStatus = HttpStatus.OK, var errorMessage: String = null, var result: Object = null)

object Utils {

  def decorateMainPage(model: Model, mainPage: String, fragmentId: String): String = {
    model.addAttribute("mainPage", mainPage)
    model.addAttribute("fragmentId", fragmentId)
    "index"
  }

  def fileEntityToFileForm(fileEntity: File): FileForm = {
    val file = new FileForm
    if (fileEntity != null) {
      file.setId(fileEntity.id)
      file.setFileName(fileEntity.fileName)
      file.setFilePath(fileEntity.savedPath)
      file.setCategoryId(fileEntity.category.id)
      file.setSelectedFilePermissions(permissionsToList(fileEntity.filePermissions))
    }
    file
  }

  def permissionsToString(permissions: List[PosixFilePermission]): String = {
    permissions.mkString(",")
  }

  def permissionsToList(permissions: String): Array[PosixFilePermission] = {
    permissions match {
      case null => Array.empty
      case "" => Array.empty
      case _ => permissions.split(",").toSeq.map(PosixFilePermission.valueOf).toArray
    }
  }

  def deleteFileByPath(pathStr: String): String = {
    if (Files.deleteIfExists(Paths.get(pathStr))) {
      s"File in $pathStr is removed successfully"
    } else {
      s"File in $pathStr could not be removed"
    }
  }

  def buildResponse(resp: ServiceResp): ResponseEntity[Map[String, Object]] = {
    var body = Map("result" -> resp.result)
    if (resp.status != HttpStatus.OK) {
      body = errorMap(resp.errorMessage)
    }
    ResponseEntity.status(resp.status).body(body)
  }

  def errorResponse(status: HttpStatus, error: String): ResponseEntity[Object] = {
    ResponseEntity.status(status).body(errorMap(error))
  }

  def errorMap(error: String): Map[String, Object] = {
    Map("errorMessage" -> error)
  }
}