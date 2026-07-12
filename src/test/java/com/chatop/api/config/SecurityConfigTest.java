package com.chatop.api.config;

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
    void rentalUploadPictureIsServedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/uploads/rentals/online-house-rental-sites.jpg"))
            .andExpect(status().isOk())
            .andExpect(content().bytes(PICTURE_CONTENT));
    }
}
