package com.chatop.api.config.properties;

import java.util.List;

public final class ChatopPropertiesTestFactory {

    private ChatopPropertiesTestFactory() {
    }

    public static ChatopProperties defaultProperties() {
        return properties(uploadsProperties());
    }

    public static ChatopProperties propertiesWithUploadsDir(String uploadsDir) {
        UploadsProperties uploads = uploadsProperties();
        uploads.setRentalsDir(uploadsDir);

        return properties(uploads);
    }

    private static ChatopProperties properties(UploadsProperties uploads) {
        return new ChatopProperties(
            jwtProperties(),
            uploads,
            mailProperties(),
            rentalMessageMailProperties(),
            openApiProperties(),
            systemProperties(),
            responseMessagesProperties(),
            errorMessagesProperties()
        );
    }

    private static JwtProperties jwtProperties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("dev-secret-key-change-me-please-dev-secret-key");
        properties.setExpirationSeconds(86400);

        return properties;
    }

    private static UploadsProperties uploadsProperties() {
        UploadsProperties properties = new UploadsProperties();
        properties.setRentalsDir("uploads/rentals");
        properties.setRentalsUrlPath("/api/uploads/rentals/");
        properties.setAllowedPictureExtensions(List.of("jpg", "jpeg", "png", "webp"));

        return properties;
    }

    private static MailProperties mailProperties() {
        MailProperties properties = new MailProperties();
        properties.setFrom("no-reply@chatop.local");

        return properties;
    }

    private static RentalMessageMailProperties rentalMessageMailProperties() {
        RentalMessageMailProperties properties = new RentalMessageMailProperties();
        properties.setSubjectTemplate("New message for your rental: %s");
        properties.setBodyTemplate("""
            Hello,

            %s (%s) sent you a message about your rental "%s":

            %s
            """);
        properties.setDeliveryFailureLog("Unable to send rental message email notification");

        return properties;
    }

    private static OpenApiProperties openApiProperties() {
        OpenApiProperties properties = new OpenApiProperties();
        properties.setTitle("Châtop API");
        properties.setDescription("Backend REST API for the Châtop project.");
        properties.setVersion("v1");

        return properties;
    }

    private static SystemProperties systemProperties() {
        SystemProperties properties = new SystemProperties();
        properties.setName("Châtop API");
        properties.setStatus("running");
        properties.setHealthPath("/api/health");
        properties.setHealthStatus("OK");

        return properties;
    }

    private static ResponseMessagesProperties responseMessagesProperties() {
        ResponseMessagesProperties properties = new ResponseMessagesProperties();
        properties.setRentalCreated("Rental created !");
        properties.setRentalUpdated("Rental updated !");
        properties.setMessageSent("Message send with success");

        return properties;
    }

    private static ErrorMessagesProperties errorMessagesProperties() {
        ErrorMessagesProperties properties = new ErrorMessagesProperties();
        properties.setEmailAlreadyExists("Email already exists");
        properties.setInvalidCredentials("Invalid credentials");
        properties.setUnauthorized("Unauthorized");
        properties.setUserNotFound("User not found");
        properties.setRentalNotFound("Rental not found");
        properties.setForbidden("Forbidden");
        properties.setInvalidUserId("Invalid user_id");
        properties.setInvalidRentalId("Invalid rental_id");
        properties.setPictureFileRequired("Picture must be sent as a file");
        properties.setInvalidPictureFilename("Invalid picture filename");
        properties.setCouldNotStorePicture("Could not store picture");
        properties.setCouldNotDeletePicture("Could not delete picture");
        properties.setPictureRequired("Picture is required");
        properties.setPictureExtensionRequired("Picture extension is required");
        properties.setPictureFormatNotSupported("Picture format is not supported");
        properties.setPictureMustBeImage("Picture must be an image");

        return properties;
    }
}
