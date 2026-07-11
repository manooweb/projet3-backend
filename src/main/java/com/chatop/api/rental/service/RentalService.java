package com.chatop.api.rental.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.rental.dto.CreateRentalRequest;
import com.chatop.api.rental.dto.RentalResponse;
import com.chatop.api.rental.dto.RentalSummaryResponse;
import com.chatop.api.rental.dto.RentalsResponse;
import com.chatop.api.rental.dto.UpdateRentalRequest;
import com.chatop.api.rental.model.Rental;
import com.chatop.api.rental.repository.RentalRepository;
import com.chatop.api.user.model.User;
import com.chatop.api.user.repository.UserRepository;

@Service
public class RentalService {

    private static final String RENTAL_CREATED_MESSAGE = "Rental created !";
    private static final String RENTAL_UPDATED_MESSAGE = "Rental updated !";

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final RentalPictureStorageService rentalPictureStorageService;

    public RentalService(
        RentalRepository rentalRepository,
        UserRepository userRepository,
        RentalPictureStorageService rentalPictureStorageService
    ) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.rentalPictureStorageService = rentalPictureStorageService;
    }

    @Transactional(readOnly = true)
    public RentalsResponse findAll() {
        return new RentalsResponse(
            rentalRepository.findAll().stream()
                .map(this::toSummaryResponse)
                .toList()
        );
    }

    @Transactional(readOnly = true)
    public RentalSummaryResponse findById(Integer id) {
        return rentalRepository.findById(id)
            .map(this::toSummaryResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found"));
    }

    @Transactional
    public RentalResponse create(CreateRentalRequest request, Authentication authentication) {
        User owner = userRepository.findById(userIdFromToken(authentication))
            .orElseThrow(this::unauthorized);
        String pictureUrl = rentalPictureStorageService.store(request.getPicture());

        Rental rental = new Rental(
            request.getName().trim(),
            request.getSurface(),
            request.getPrice(),
            pictureUrl,
            request.getDescription().trim(),
            owner
        );

        rentalRepository.save(rental);

        return new RentalResponse(RENTAL_CREATED_MESSAGE);
    }

    @Transactional
    public RentalResponse update(Integer id, UpdateRentalRequest request, Authentication authentication) {
        Rental rental = rentalRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found"));

        if (!rental.getOwner().getId().equals(userIdFromToken(authentication))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        String oldPictureUrl = rental.getPicture();
        String pictureUrl = oldPictureUrl;

        if (request.getPicture() != null && !request.getPicture().isEmpty()) {
            pictureUrl = rentalPictureStorageService.store(request.getPicture());
        }

        rental.update(
            request.getName().trim(),
            request.getSurface(),
            request.getPrice(),
            pictureUrl,
            request.getDescription().trim()
        );

        rentalRepository.save(rental);

        if (!Objects.equals(pictureUrl, oldPictureUrl)) {
            rentalPictureStorageService.delete(oldPictureUrl);
        }

        return new RentalResponse(RENTAL_UPDATED_MESSAGE);
    }

    private RentalSummaryResponse toSummaryResponse(Rental rental) {
        return new RentalSummaryResponse(
            rental.getId(),
            rental.getName(),
            rental.getSurface(),
            rental.getPrice(),
            rental.getPicture(),
            rental.getDescription(),
            rental.getOwner().getId(),
            rental.getCreatedAt(),
            rental.getUpdatedAt()
        );
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
