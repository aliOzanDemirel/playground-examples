package archiver.app.config

import archiver.app.common.Mappings
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.firewall.StrictHttpFirewall

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class SecurityConf extends WebSecurityConfigurerAdapter {

  val log: Logger = LoggerFactory.getLogger(classOf[SecurityConf])

  @Value("${app.enabled.csrf}")
  var enableCsrf: Boolean = _

  @Value("${app.enabled.in-memory-auth}")
  var enableInMemAuth: Boolean = _

  @Value("${app.enabled.security}")
  var enableSecurity: Boolean = _

  @Value("${app.enabled.headers}")
  var enableHeaders: Boolean = _

  @Value("${spring.h2.console.path}")
  var h2ConsolePath: String = _

  override def configure(http: HttpSecurity): Unit = {

    http.authorizeRequests()
      .antMatchers("/static/**", "/webjars/**", "/api/**").permitAll()
      .antMatchers("/admin/**").hasRole("ADMIN")
      .requestMatchers(PathRequest.toH2Console).access("hasRole('ADMIN') and hasRole('DBA')")
      .anyRequest.authenticated()
      .and()
      .formLogin()
      .usernameParameter(Mappings.LOGIN_USER_PARAM_NAME)
      .passwordParameter(Mappings.LOGIN_PASSWORD_PARAM_NAME)
      .loginPage("/")
      .loginProcessingUrl(Mappings.LOGIN_ACTION_ENDPOINT).permitAll()
      .defaultSuccessUrl(Mappings.ALL_FILES)
      .and()
      .logout().logoutSuccessUrl("/?logged-out")
      .and()
      .exceptionHandling().accessDeniedPage("/")
      .and()
      .sessionManagement().maximumSessions(1)

    if (enableCsrf) {
      // h2-console in iframe cannot do post requests if csrf is enabled
      http.csrf().ignoringAntMatchers("/api/**").ignoringAntMatchers(h2ConsolePath + "/**")
      log.info("csrf is enabled")
    } else {
      disableCSRF()
    }

    if (enableHeaders) {
      log.info("security headers are enabled")
    } else {
      http.headers().disable()
      log.warn("security headers are disabled")
    }

    def disableCSRF(): Unit = {
      http.csrf().disable()
      log.warn("csrf is disabled")
    }
  }

  override def configure(web: WebSecurity): Unit = {
    if (enableSecurity) {
      web.ignoring.antMatchers("/static/**", "/webjars/**")
    } else {
      web.ignoring.antMatchers("/**")
    }

    web.httpFirewall(notMuchOfAFireWall())

    def notMuchOfAFireWall() = {
      val firewall = new StrictHttpFirewall()
      firewall.setAllowUrlEncodedSlash(true)
      firewall.setAllowUrlEncodedPercent(true)
      firewall.setAllowUrlEncodedPeriod(true)
      firewall.setAllowSemicolon(true)
      firewall.setAllowBackSlash(true)
      firewall
    }
  }

  @Bean def passwordEncoder = new BCryptPasswordEncoder

  override def configure(auth: AuthenticationManagerBuilder): Unit = {
    if (enableInMemAuth) {
      auth.inMemoryAuthentication.passwordEncoder(passwordEncoder)
        .withUser("ozan")
        .password(passwordEncoder.encode("ozan"))
        .roles("USER")
        .and()
        .withUser("admin")
        .password(passwordEncoder.encode("admin"))
        .roles("USER", "ADMIN", "DBA")
    } else {
      auth.jdbcAuthentication().passwordEncoder(passwordEncoder)
    }
  }

}
