package bond.config.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.Instant;
import java.time.LocalDate;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    /**
     * custom @PagingParameters is used to document Pageable parameters instead of AlternateTypeRuleConvention.
     * swagger's @ApiIgnore is used to ignore actual Pageable in parameter list.
     */

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("simple.bond.issuer"))
                .paths(regex("/api/.*"))
                .build()
                .apiInfo(metaData())
                .directModelSubstitute(LocalDate.class, String.class)
                .directModelSubstitute(Instant.class, String.class);
    }

    private ApiInfo metaData() {
        return new ApiInfoBuilder()
                .title("Simple Bond Issuer API")
                .build();
    }

}
