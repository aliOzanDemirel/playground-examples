package app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class PageConfig implements WebMvcConfigurer {

    public static final String[] PAGES = {"/", "/forecast", "/estimation", "/sign-up"};

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        String mainPageToForward = "forward:/index.html";
        Arrays.stream(PAGES).map(registry::addViewController).forEach(it -> it.setViewName(mainPageToForward));
    }

}
