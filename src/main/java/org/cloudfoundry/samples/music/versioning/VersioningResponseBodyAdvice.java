package org.cloudfoundry.samples.music.versioning;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class VersioningResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private VersioningManager versioningManager;

    public VersioningResponseBodyAdvice(VersioningManager versioningManager) {
        this.versioningManager = versioningManager;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        return this.versioningManager.beforeBodyWrite(body, request.getHeaders());
    }

}
