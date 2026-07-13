package com.chatop.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;

import com.chatop.api.config.properties.ChatopPropertiesTestFactory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void authenticationRequestExamplesUseConfiguredDemoUser() {
        OpenAPI openApi = new OpenAPI()
            .path("/api/auth/login", new PathItem().post(operationWithJsonRequestBody()))
            .path("/api/auth/register", new PathItem().post(operationWithJsonRequestBody()));

        OpenApiCustomizer customizer = config.authenticationRequestExamples(ChatopPropertiesTestFactory.defaultProperties());

        customizer.customise(openApi);

        assertThat(jsonExample(openApi, "/api/auth/login"))
            .isEqualTo(Map.of("email", "demo@chatop.com", "password", "password"));
        assertThat(jsonExample(openApi, "/api/auth/register"))
            .isEqualTo(Map.of("name", "Demo User", "email", "demo@chatop.com", "password", "password"));
    }

    private Operation operationWithJsonRequestBody() {
        return new Operation()
            .requestBody(new RequestBody()
                .content(new Content()
                    .addMediaType("application/json", new MediaType())));
    }

    private Object jsonExample(OpenAPI openApi, String path) {
        return openApi.getPaths()
            .get(path)
            .getPost()
            .getRequestBody()
            .getContent()
            .get("application/json")
            .getExample();
    }
}
