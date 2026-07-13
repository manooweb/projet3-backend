package com.chatop.api.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;

@SpringBootTest(properties = "chatop.uploads.rentals-dir=target/test-uploads/static-resources")
@AutoConfigureMockMvc
class SecurityConfigTest {

    private static final Path RENTAL_UPLOADS_PATH = Path.of("target/test-uploads/static-resources");
    private static final byte[] PICTURE_CONTENT = "image-content".getBytes(StandardCharsets.UTF_8);

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(RENTAL_UPLOADS_PATH);
        Files.write(RENTAL_UPLOADS_PATH.resolve("online-house-rental-sites.jpg"), PICTURE_CONTENT);
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(RENTAL_UPLOADS_PATH);
    }

    @Test
    void protectedEndpointWithoutTokenReturnsApiErrorResponse() throws Exception {
        mockMvc.perform(get("/api/rentals"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status", is(401)))
            .andExpect(jsonPath("$.error", is("Unauthorized")))
            .andExpect(jsonPath("$.message", is("Unauthorized")))
            .andExpect(jsonPath("$.path", is("/api/rentals")));
    }

    @Test
    void csrfEndpointCreatesReadableXsrfCookie() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
            .andExpect(status().isNoContent())
            .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    void unsafeRequestWithoutCsrfTokenReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("""
                    {
                      "email": "test@example.com",
                      "password": "password"
                    }
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status", is(403)))
            .andExpect(jsonPath("$.error", is("Forbidden")))
            .andExpect(jsonPath("$.message", is("Forbidden")))
            .andExpect(jsonPath("$.path", is("/api/auth/login")));
    }

    @Test
    void rootPageIsServedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("ChâTop API")))
            .andExpect(content().string(containsString("/swagger-ui.html")))
            .andExpect(content().string(containsString("/api/health")))
            .andExpect(content().string(containsString("target=\"_blank\"")))
            .andExpect(content().string(containsString("Check database status")))
            .andExpect(content().string(containsString("Check database schema")))
            .andExpect(content().string(containsString("/js/home-status.js")))
            .andExpect(content().string(containsString("API status:")));
    }

    @Test
    void healthEndpointReturnsApplicationAndDatabaseStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.application.name", is("Châtop API")))
            .andExpect(jsonPath("$.application.status", is("OK")))
            .andExpect(jsonPath("$.application.version").exists())
            .andExpect(jsonPath("$.application.timestamp").exists())
            .andExpect(jsonPath("$.database.status").exists())
            .andExpect(jsonPath("$.database.timestamp").exists());
    }

    @Test
    void schemaHealthEndpointIsServedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/health/schema"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.missing").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void homeStatusScriptIsServedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/js/home-status.js"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("getJson('/api/health')")))
            .andExpect(content().string(containsString("getJson('/api/health/schema')")))
            .andExpect(content().string(containsString("database schema invalid")))
            .andExpect(content().string(containsString("setCheckButtonsHidden(true)")))
            .andExpect(content().string(containsString("setDatabaseStatusButtonHidden(true)")))
            .andExpect(content().string(containsString("setSchemaButtonHidden(true)")))
            .andExpect(content().string(containsString("addEventListener('click', checkStatus)")))
            .andExpect(content().string(containsString("API OK, database unavailable")));
    }

    @Test
    void swaggerUiIndexIncludesHomeLinkScript() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/js/swagger-home-link.js")));
    }

    @Test
    void swaggerHomeLinkScriptIsServedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/js/swagger-home-link.js"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(".renderedMarkdown a[href=\"/\"]")))
            .andExpect(content().string(containsString("text-decoration: none")))
            .andExpect(content().string(containsString("text-decoration: underline")));
    }

    @Test
    void rentalUploadPictureIsServedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/uploads/rentals/online-house-rental-sites.jpg"))
            .andExpect(status().isOk())
            .andExpect(content().bytes(PICTURE_CONTENT));
    }
}
