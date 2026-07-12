package com.chatop.api.rental.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.config.properties.ChatopPropertiesTestFactory;

class RentalPictureStorageServiceTest {

    @TempDir
    private Path uploadsPath;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void storeWritesPictureAndReturnsPublicUrl() throws Exception {
        mockCurrentRequest();
        RentalPictureStorageService storageService = storageService();

        String pictureUrl = storageService.store(new MockMultipartFile(
            "picture",
            "Belle Maison d'été 2026!.JPG",
            "image/jpeg",
            "image-content".getBytes()
        ));

        String filename = Path.of(URI.create(pictureUrl).getPath()).getFileName().toString();

        assertThat(pictureUrl).startsWith("http://localhost:9001/api/uploads/rentals/");
        assertThat(filename).isEqualTo("belle-maison-d-ete-2026.jpg");
        assertThat(Files.readString(uploadsPath.resolve(filename))).isEqualTo("image-content");
    }

    @Test
    void storeAddsNumericSuffixWhenNormalizedFilenameAlreadyExists() throws Exception {
        mockCurrentRequest();
        RentalPictureStorageService storageService = storageService();

        storageService.store(new MockMultipartFile(
            "picture",
            "house.jpg",
            "image/jpeg",
            "first-image-content".getBytes()
        ));

        String pictureUrl = storageService.store(new MockMultipartFile(
            "picture",
            "house.jpg",
            "image/jpeg",
            "second-image-content".getBytes()
        ));

        String filename = Path.of(URI.create(pictureUrl).getPath()).getFileName().toString();

        assertThat(filename).isEqualTo("house-2.jpg");
        assertThat(Files.readString(uploadsPath.resolve("house.jpg"))).isEqualTo("first-image-content");
        assertThat(Files.readString(uploadsPath.resolve("house-2.jpg"))).isEqualTo("second-image-content");
    }

    @Test
    void storeThrowsBadRequestWhenFileIsNotAnImage() {
        mockCurrentRequest();
        RentalPictureStorageService storageService = storageService();

        assertThatThrownBy(() -> storageService.store(new MockMultipartFile(
                "picture",
                "house.txt",
                "text/plain",
                "not-an-image".getBytes()
            )))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteRemovesLocalPictureFromPublicUrl() throws Exception {
        Path picturePath = uploadsPath.resolve("house.jpg");
        Files.writeString(picturePath, "image-content");
        RentalPictureStorageService storageService = storageService();

        storageService.delete("http://localhost:9001/api/uploads/rentals/house.jpg");

        assertThat(Files.exists(picturePath)).isFalse();
    }

    @Test
    void deleteIgnoresExternalPictureUrl() throws Exception {
        Path picturePath = uploadsPath.resolve("house.jpg");
        Files.writeString(picturePath, "image-content");
        RentalPictureStorageService storageService = storageService();

        storageService.delete("https://example.com/uploads/house.jpg");

        assertThat(Files.exists(picturePath)).isTrue();
    }

    private void mockCurrentRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(9001);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private RentalPictureStorageService storageService() {
        return new RentalPictureStorageService(ChatopPropertiesTestFactory.propertiesWithUploadsDir(uploadsPath.toString()));
    }
}
