package bond.controller.advice;

import bond.dto.wrapper.ContentWrapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class NoContentAdvice implements ResponseBodyAdvice<ContentWrapper> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {

        return ContentWrapper.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public ContentWrapper beforeBodyWrite(ContentWrapper body,
                                          MethodParameter returnType,
                                          MediaType selectedContentType,
                                          Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                          ServerHttpRequest request,
                                          ServerHttpResponse response) {

        if (body.getContent().isEmpty()) {
            response.setStatusCode(HttpStatus.NO_CONTENT);
        }
        return body;
    }

}
