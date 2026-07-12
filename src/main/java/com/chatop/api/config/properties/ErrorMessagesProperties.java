package com.chatop.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "chatop.errors")
public class ErrorMessagesProperties {

    private String emailAlreadyExists;
    private String invalidCredentials;
    private String unauthorized;
    private String userNotFound;
    private String rentalNotFound;
    private String forbidden;
    private String invalidUserId;
    private String invalidRentalId;
    private String pictureFileRequired;
    private String invalidPictureFilename;
    private String couldNotStorePicture;
    private String couldNotDeletePicture;
    private String pictureRequired;
    private String pictureExtensionRequired;
    private String pictureFormatNotSupported;
    private String pictureMustBeImage;
}
