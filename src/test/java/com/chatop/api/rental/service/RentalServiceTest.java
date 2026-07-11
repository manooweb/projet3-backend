package com.chatop.api.rental.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.rental.dto.CreateRentalRequest;
import com.chatop.api.rental.dto.RentalResponse;
import com.chatop.api.rental.model.Rental;
import com.chatop.api.rental.repository.RentalRepository;
import com.chatop.api.user.model.User;
import com.chatop.api.user.repository.UserRepository;

class RentalServiceTest {

    private RentalRepository rentalRepository;
    private UserRepository userRepository;
    private RentalService rentalService;

    @BeforeEach
    void setUp() {
        rentalRepository = mock(RentalRepository.class);
        userRepository = mock(UserRepository.class);
        rentalService = new RentalService(rentalRepository, userRepository);
    }

    @Test
    void createSavesRentalForAuthenticatedUser() {
        User owner = new User("test@example.com", "Test", "encoded-password");

        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalResponse response = rentalService.create(validRequest(), authenticationWithUserId(1));

        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository).save(rentalCaptor.capture());

        Rental savedRental = rentalCaptor.getValue();

        assertThat(response.message()).isEqualTo("Rental created !");
        assertThat(savedRental.getName()).isEqualTo("House");
        assertThat(savedRental.getSurface()).isEqualByComparingTo("120");
        assertThat(savedRental.getPrice()).isEqualByComparingTo("950");
        assertThat(savedRental.getDescription()).isEqualTo("A nice house");
        assertThat(savedRental.getOwner()).isSameAs(owner);
        assertThat(savedRental.getPicture()).startsWith("https://");
    }

    @Test
    void createThrowsBadRequestWhenPictureIsEmpty() {
        CreateRentalRequest request = validRequest();
        request.setPicture(new MockMultipartFile("picture", new byte[0]));

        assertThatThrownBy(() -> rentalService.create(request, authenticationWithUserId(1)))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    void createThrowsUnauthorizedWhenUserDoesNotExist() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.create(validRequest(), authenticationWithUserId(1)))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.UNAUTHORIZED);

        verify(rentalRepository, never()).save(any(Rental.class));
    }

    private CreateRentalRequest validRequest() {
        CreateRentalRequest request = new CreateRentalRequest();
        request.setName(" House ");
        request.setSurface(new BigDecimal("120"));
        request.setPrice(new BigDecimal("950"));
        request.setPicture(new MockMultipartFile(
            "picture",
            "house.jpg",
            "image/jpeg",
            "image-content".getBytes()
        ));
        request.setDescription(" A nice house ");

        return request;
    }

    private JwtAuthenticationToken authenticationWithUserId(Integer userId) {
        return new JwtAuthenticationToken(jwtWithUserId(userId));
    }

    private Jwt jwtWithUserId(Integer userId) {
        Instant now = Instant.now();

        return new Jwt(
            "jwt-token",
            now,
            now.plusSeconds(3600),
            Map.of("alg", "HS256"),
            Map.of("sub", "test@example.com", "userId", userId)
        );
    }
}
