package com.chatop.api.config;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.OpenApiProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String JWT_SECURITY_SCHEME = "cookieAuth";

    @Bean
    OpenAPI chatopOpenAPI(ChatopProperties chatopProperties) {
        OpenApiProperties openApiProperties = chatopProperties.getOpenApi();

        return new OpenAPI()
            .info(new Info()
                .title(openApiProperties.getTitle())
                .description(openApiProperties.getDescription())
                .version(openApiProperties.getVersion()))
            .addSecurityItem(new SecurityRequirement().addList(JWT_SECURITY_SCHEME))
            .components(new Components()
                .addSecuritySchemes(JWT_SECURITY_SCHEME, new SecurityScheme()
                    .name(chatopProperties.getJwt().getCookieName())
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.COOKIE)));
    }
}
