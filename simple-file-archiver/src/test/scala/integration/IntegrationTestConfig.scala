package integration

import archiver.app.repository.{CategoryRepo, FileRepo}
import helper.Provider
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean

@TestConfiguration
class IntegrationTestConfig @Autowired()(private val fileRepo: FileRepo,
                                         private val categoryRepo: CategoryRepo) {

  @PostConstruct
  def loadData(): Unit = {
    Provider.loadCategories(categoryRepo.save)
    Provider.loadFiles(fileRepo.save, index => categoryRepo.findById(java.lang.Long.valueOf((index % 3) + 1)).get())
  }

  @Bean
  def restTemplateBuilder: RestTemplateBuilder = new RestTemplateBuilder().setConnectTimeout(50000).setReadTimeout(5000)

}
