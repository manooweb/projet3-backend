package com.chatop.api.rental.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class RentalPictureStorageService {

    private static final String RENTAL_PICTURE_URL_PATH = "/api/uploads/rentals/";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final Path rentalUploadsPath;

    public RentalPictureStorageService(@Value("${app.uploads.rentals-dir}") String rentalUploadsDir) {
        this.rentalUploadsPath = Path.of(rentalUploadsDir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile picture) {
        validatePicture(picture);

        String filename = uniqueFilename(picture.getOriginalFilename());
        Path destination = rentalUploadsPath.resolve(filename).normalize();

        if (!destination.startsWith(rentalUploadsPath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid picture filename");
        }

        try {
            Files.createDirectories(rentalUploadsPath);

            try (InputStream inputStream = picture.getInputStream()) {
                Files.copy(inputStream, destination);
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store picture", exception);
        }

        return publicUrl(filename);
    }

    private void validatePicture(MultipartFile picture) {
        if (picture == null || picture.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Picture is required");
        }

        String extension = extension(picture.getOriginalFilename())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Picture extension is required"));

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Picture format is not supported");
        }

        String contentType = picture.getContentType();

        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Picture must be an image");
        }
    }

    private String uniqueFilename(String originalFilename) {
        String extension = extension(originalFilename)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Picture extension is required"));
        String baseName = normalizedBaseName(originalFilename);
        String filename = baseName + "." + extension;
        int suffix = 2;

        while (Files.exists(rentalUploadsPath.resolve(filename))) {
            filename = baseName + "-" + suffix + "." + extension;
            suffix++;
        }

        return filename;
    }

    private String normalizedBaseName(String originalFilename) {
        String filename = StringUtils.cleanPath(Optional.ofNullable(originalFilename).orElse(""));
        int extensionIndex = filename.lastIndexOf('.');
        String baseName = extensionIndex > 0 ? filename.substring(0, extensionIndex) : filename;
        String normalized = Normalizer.normalize(baseName, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-+|-+$)", "");

        return normalized.isBlank() ? "image" : normalized;
    }

    private Optional<String> extension(String originalFilename) {
        String filename = StringUtils.cleanPath(Optional.ofNullable(originalFilename).orElse(""));
        int extensionIndex = filename.lastIndexOf('.');

        if (extensionIndex < 0 || extensionIndex == filename.length() - 1) {
            return Optional.empty();
        }

        return Optional.of(filename.substring(extensionIndex + 1).toLowerCase(Locale.ROOT));
    }

    private String publicUrl(String filename) {
        return ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path(RENTAL_PICTURE_URL_PATH)
            .path(filename)
            .toUriString();
    }
}
