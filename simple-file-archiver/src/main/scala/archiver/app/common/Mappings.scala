package archiver.app.common

object Mappings {

  final val LOGIN_USER_PARAM_NAME = "user"
  final val LOGIN_PASSWORD_PARAM_NAME = "pass"
  final val LOGIN_ACTION_ENDPOINT = "/login"

  final val REDIRECT_TO_FILES = "redirect:/files"
  final val REDIRECT_TO_CATEGORIES = "redirect:/categories"
  final val REDIRECT_TO_FILE_FORM = "redirect:/file/form"

  final val ALL_FILES = "/files"
  final val FILE_FORM = "/file/form"
  final val SAVE_FILE = "/file/save"
  final val DELETE_FILE = "/file/delete"
  final val DOWNLOAD_FILE = "/file/download"

  final val ALL_CATEGORIES = "/categories"
  final val SAVE_CATEGORY = "/category/save"
  final val DELETE_CATEGORY = "/category/delete"

  final val ADMIN = "/admin"
  final val H2_CONSOLE = "/h2-console"

  final val API_FILES = "/api/files"
  final val API_CATEGORIES = "/api/categories"

}
