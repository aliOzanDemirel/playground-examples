package archiver.app

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.undertow.Undertow
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.{Bean, Primary}
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.{ResourceHandlerRegistry, WebMvcConfigurer}

object MainRunner {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[AppConf], args: _*)
  }
}

@SpringBootApplication
class AppConf extends WebMvcConfigurer {

  @Value("${spring.resources.static-locations}")
  var staticResourceLocation: String = _

  //  added to use static files from os path instead of jar's classpath
  override def addResourceHandlers(registry: ResourceHandlerRegistry): Unit = {
    registry.addResourceHandler("/static/**").addResourceLocations(staticResourceLocation)
    registry.addResourceHandler("/webjars/**").addResourceLocations("/webjars/")
  }

  // this is for serializing scala collections too since spring does not do it automatically
  @Bean
  @Primary
  def scalaMessageConverter(): MappingJackson2HttpMessageConverter = {
    val converter = new MappingJackson2HttpMessageConverter
    converter.setObjectMapper(objectMapper())
    converter
  }

  def objectMapper(): ObjectMapper = {
    new ObjectMapper()
      .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
      .registerModule(DefaultScalaModule)
      .registerModule(new JavaTimeModule)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  }

  // http clear text listener should be added manually
  @Bean
  def servletContainer(@Value("${app.http.port}") httpPort: Int): ServletWebServerFactory = {
    val undertow = new UndertowServletWebServerFactory
    undertow.addBuilderCustomizers((builder: Undertow.Builder) => builder.addHttpListener(httpPort, "0.0.0.0"))
    undertow
  }

}
