package com.chatop.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String JWT_SECURITY_SCHEME = "bearerAuth";

    @Bean
    OpenAPI chatopOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Châtop API")
                .description("Backend REST API for the Châtop project.")
                .version("v1"))
            .addSecurityItem(new SecurityRequirement().addList(JWT_SECURITY_SCHEME))
            .components(new Components()
                .addSecuritySchemes(JWT_SECURITY_SCHEME, new SecurityScheme()
                    .name(JWT_SECURITY_SCHEME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
