package app.config.security;

import app.config.security.jwt.JwtConfigurer;
import app.config.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static app.config.PageConfig.PAGES;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.profiles.active}")
    private String env;

    @Autowired
    private ApiAccessDeniedHandler apiAccessDeniedHandler;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // disable CSRF (cross site request forgery)
        http.csrf().disable();

        // no session will be created or used by spring security
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = http.authorizeRequests();

        registry.antMatchers(HttpMethod.POST, "/users/sign-in").permitAll()
                .antMatchers(HttpMethod.POST, "/users/sign-up").permitAll()
                .antMatchers(HttpMethod.GET, "/users", "/users/highscores", "/timeseries").permitAll()
                // disallow everything else
                .anyRequest().authenticated();

        if ("local".equals(env)) {
            registry.antMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            http.cors();
        }

        http.exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler(apiAccessDeniedHandler);

        // apply JWT
        http.apply(new JwtConfigurer(jwtTokenProvider));
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/ws/**")
                .antMatchers(HttpMethod.GET, "/static/**", "/favicon.ico", "/manifest.json", "/asset-manifest.json")
                .antMatchers(HttpMethod.GET, PAGES);
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedPeriod(true);
        return firewall;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @Profile("local")
    public CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        config.addAllowedOrigin("https://localhost:3000");
        config.addAllowedOrigin("http://localhost:3000");
        config.setAllowedHeaders(Arrays.asList(HttpHeaders.CONTENT_TYPE, JwtTokenProvider.AUTH, HttpHeaders.COOKIE));
        config.setExposedHeaders(Arrays.asList(JwtTokenProvider.AUTH, HttpHeaders.SET_COOKIE));
        config.addAllowedMethod(HttpMethod.OPTIONS.name());
        config.addAllowedMethod(HttpMethod.POST.name());
        config.addAllowedMethod(HttpMethod.GET.name());
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
