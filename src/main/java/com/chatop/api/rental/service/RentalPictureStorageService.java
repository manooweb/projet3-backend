package com.chatop.api.rental.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.ErrorMessagesProperties;
import com.chatop.api.config.properties.UploadsProperties;

@Service
public class RentalPictureStorageService {

    private final Path rentalUploadsPath;
    private final String rentalPictureUrlPath;
    private final Set<String> allowedExtensions;
    private final ErrorMessagesProperties errors;

    public RentalPictureStorageService(ChatopProperties chatopProperties) {
        UploadsProperties uploads = chatopProperties.getUploads();

        this.rentalUploadsPath = Path.of(uploads.getRentalsDir()).toAbsolutePath().normalize();
        this.rentalPictureUrlPath = uploads.getRentalsUrlPathWithTrailingSlash();
        this.allowedExtensions = uploads.getAllowedPictureExtensions().stream()
            .map(extension -> extension.toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
        this.errors = chatopProperties.getErrors();
    }

    public String store(MultipartFile picture) {
        validatePicture(picture);

        String filename = uniqueFilename(picture.getOriginalFilename());
        Path destination = rentalUploadsPath.resolve(filename).normalize();

        if (!destination.startsWith(rentalUploadsPath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.getInvalidPictureFilename());
        }

        try {
            Files.createDirectories(rentalUploadsPath);

            try (InputStream inputStream = picture.getInputStream()) {
                Files.copy(inputStream, destination);
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errors.getCouldNotStorePicture(), exception);
        }

        return publicUrl(filename);
    }

    public void delete(String pictureUrl) {
        Optional<Path> picturePath = localPicturePath(pictureUrl);

        if (picturePath.isEmpty()) {
            return;
        }

        try {
            Files.deleteIfExists(picturePath.get());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errors.getCouldNotDeletePicture(), exception);
        }
    }

    private void validatePicture(MultipartFile picture) {
        if (picture == null || picture.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.getPictureRequired());
        }

        String extension = extension(picture.getOriginalFilename())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.getPictureExtensionRequired()));

        if (!allowedExtensions.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.getPictureFormatNotSupported());
        }

        String contentType = picture.getContentType();

        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.getPictureMustBeImage());
        }
    }

    private String uniqueFilename(String originalFilename) {
        String extension = extension(originalFilename)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.getPictureExtensionRequired()));
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

    private Optional<Path> localPicturePath(String pictureUrl) {
        if (pictureUrl == null || pictureUrl.isBlank()) {
            return Optional.empty();
        }

        String path;

        try {
            path = new URI(pictureUrl).getPath();
        } catch (URISyntaxException exception) {
            return Optional.empty();
        }

        if (path == null || !path.startsWith(rentalPictureUrlPath)) {
            return Optional.empty();
        }

        String filename = Path.of(path).getFileName().toString();
        Path picturePath = rentalUploadsPath.resolve(filename).normalize();

        if (!picturePath.startsWith(rentalUploadsPath)) {
            return Optional.empty();
        }

        return Optional.of(picturePath);
    }

    private String publicUrl(String filename) {
        return ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path(rentalPictureUrlPath)
            .path(filename)
            .toUriString();
    }

}
