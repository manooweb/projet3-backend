package com.chatop.api.config;

import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.OpenApiProperties;
import com.chatop.api.config.properties.SwaggerDemoUserProperties;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    public static final String COOKIE_AUTH_SECURITY_SCHEME = "cookieAuth";
    private static final String JSON_MEDIA_TYPE = "application/json";

    @Bean
    OpenAPI chatopOpenAPI(ChatopProperties chatopProperties) {
        OpenApiProperties openApiProperties = chatopProperties.getOpenApi();

        return new OpenAPI()
            .info(new Info()
                .title(openApiProperties.getTitle())
                .description(openApiProperties.getDescription())
                .version(openApiProperties.getVersion()))
            .addSecurityItem(new SecurityRequirement().addList(COOKIE_AUTH_SECURITY_SCHEME))
            .components(new Components()
                .addSecuritySchemes(COOKIE_AUTH_SECURITY_SCHEME, new SecurityScheme()
                    .name(chatopProperties.getJwt().getCookieName())
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.COOKIE)));
    }

    @Bean
    OpenApiCustomizer authenticationRequestExamples(ChatopProperties chatopProperties) {
        SwaggerDemoUserProperties demoUser = chatopProperties.getSwaggerDemoUser();

        return openApi -> {
            if (!hasDemoUserExample(demoUser)) {
                return;
            }

            setPostJsonExample(
                openApi,
                "/api/auth/login",
                Map.of(
                    "email", demoUser.getEmail(),
                    "password", demoUser.getPassword()
                )
            );
            setPostJsonExample(
                openApi,
                "/api/auth/register",
                Map.of(
                    "name", demoUser.getName(),
                    "email", demoUser.getEmail(),
                    "password", demoUser.getPassword()
                )
            );
        };
    }

    private boolean hasDemoUserExample(SwaggerDemoUserProperties demoUser) {
        return StringUtils.hasText(demoUser.getName())
            && StringUtils.hasText(demoUser.getEmail())
            && StringUtils.hasText(demoUser.getPassword());
    }

    private void setPostJsonExample(OpenAPI openApi, String path, Map<String, String> example) {
        if (openApi.getPaths() == null) {
            return;
        }

        PathItem pathItem = openApi.getPaths().get(path);
        if (pathItem == null) {
            return;
        }

        Operation operation = pathItem.getPost();
        if (operation == null
            || operation.getRequestBody() == null
            || operation.getRequestBody().getContent() == null) {
            return;
        }

        MediaType mediaType = operation.getRequestBody().getContent().get(JSON_MEDIA_TYPE);
        if (mediaType != null) {
            mediaType.setExample(example);
        }
    }
}
