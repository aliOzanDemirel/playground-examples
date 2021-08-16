package archiver.app.controller

import archiver.app.common.Utils
import archiver.app.config.SecurityConf
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AdminController @Autowired()(private val securityConf: SecurityConf) {

  @Value("${spring.resources.static-locations}")
  var resourceLocationPath: String = _

  @Value("${spring.thymeleaf.prefix}")
  var thymeleafPrefixPath: String = _

  @Value("${server.ssl.key-store}")
  var serverKeyStorePath: String = _

  @Value("${server.undertow.accesslog.dir}")
  var logDirectory: String = _

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(Array("/admin"))
  def showAdmin(model: Model): String = {

    model.addAttribute("csrfEnabled", securityConf.enableCsrf)
    model.addAttribute("inMemAuthEnabled", securityConf.enableInMemAuth)
    model.addAttribute("resourceLocationPath", resourceLocationPath)
    model.addAttribute("thymeleafPrefixPath", thymeleafPrefixPath)
    model.addAttribute("serverKeyStorePath", serverKeyStorePath)
    model.addAttribute("logDirectory", logDirectory)

    Utils.decorateMainPage(model, "admin/list-configs", "configs")
  }

}
