package com.chatop.api.rental.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.rental.dto.CreateRentalRequest;
import com.chatop.api.rental.dto.RentalResponse;
import com.chatop.api.rental.model.Rental;
import com.chatop.api.rental.repository.RentalRepository;
import com.chatop.api.user.model.User;
import com.chatop.api.user.repository.UserRepository;

@Service
public class RentalService {

    private static final String RENTAL_CREATED_MESSAGE = "Rental created !";
    private static final String TEMPORARY_PICTURE_URL =
        "https://blog.technavio.org/wp-content/uploads/2018/12/Online-House-Rental-Sites.jpg";

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;

    public RentalService(RentalRepository rentalRepository, UserRepository userRepository) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RentalResponse create(CreateRentalRequest request, Authentication authentication) {
        validatePicture(request.getPicture());

        User owner = userRepository.findById(userIdFromToken(authentication))
            .orElseThrow(this::unauthorized);

        Rental rental = new Rental(
            request.getName().trim(),
            request.getSurface(),
            request.getPrice(),
            temporaryPictureUrl(),
            request.getDescription().trim(),
            owner
        );

        rentalRepository.save(rental);

        return new RentalResponse(RENTAL_CREATED_MESSAGE);
    }

    private void validatePicture(MultipartFile picture) {
        if (picture == null || picture.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Picture is required");
        }
    }

    private String temporaryPictureUrl() {
        // Replaced by the real upload URL when file storage is implemented.
        return TEMPORARY_PICTURE_URL;
    }

    private Integer userIdFromToken(Authentication authentication) {
        return Optional.ofNullable(authentication)
            .map(Authentication::getPrincipal)
            .filter(Jwt.class::isInstance)
            .map(Jwt.class::cast)
            .map(this::userIdFromToken)
            .orElseThrow(this::unauthorized);
    }

    private Integer userIdFromToken(Jwt jwt) {
        Object userIdClaim = jwt.getClaims().get("userId");

        if (userIdClaim instanceof Number number) {
            return number.intValue();
        }

        if (userIdClaim instanceof String userId) {
            try {
                return Integer.valueOf(userId);
            } catch (NumberFormatException exception) {
                throw unauthorized();
            }
        }

        throw unauthorized();
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
}
