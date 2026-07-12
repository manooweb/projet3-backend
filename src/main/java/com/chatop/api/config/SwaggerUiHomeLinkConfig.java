package com.chatop.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class SwaggerUiHomeLinkConfig implements BeanPostProcessor {

    private static final String HOME_LINK_JS_PATH = "/js/swagger-home-link.js";

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (!(bean instanceof SwaggerIndexTransformer delegate)) {
            return bean;
        }

        return new SwaggerIndexTransformer() {

            @Override
            public Resource transform(
                HttpServletRequest request,
                Resource resource,
                ResourceTransformerChain transformerChain
            ) throws IOException {
                Resource transformed = delegate.transform(request, resource, transformerChain);

                if (!isSwaggerIndex(request, transformed)) {
                    return transformed;
                }

                String html;
                try (InputStream inputStream = transformed.getInputStream()) {
                    html = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                }

                if (html.contains(HOME_LINK_JS_PATH) || !html.contains("</body>")) {
                    return transformed;
                }

                String scriptTag = "<script src=\"" + HOME_LINK_JS_PATH + "\" defer></script>";
                String updatedHtml = html.replace("</body>", scriptTag + "\n</body>");

                return new TransformedResource(transformed, updatedHtml.getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    private boolean isSwaggerIndex(HttpServletRequest request, Resource resource) {
        String requestUri = request.getRequestURI();
        String filename = resource.getFilename();

        return (requestUri != null
            && (requestUri.endsWith("/swagger-ui/index.html") || requestUri.endsWith("/swagger-ui.html")))
            || (filename != null && filename.equalsIgnoreCase("index.html"));
    }
}
