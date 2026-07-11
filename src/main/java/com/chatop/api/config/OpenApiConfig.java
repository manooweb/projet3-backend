package com.chatop.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI chatopOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Châtop API")
                .description("Backend REST API for the Châtop project.")
                .version("v1"));
    }
}
