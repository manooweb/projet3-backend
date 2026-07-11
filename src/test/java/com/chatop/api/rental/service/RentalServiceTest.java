package com.chatop.api.rental.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
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

class RentalServiceTest {

    private RentalRepository rentalRepository;
    private UserRepository userRepository;
    private RentalPictureStorageService rentalPictureStorageService;
    private RentalService rentalService;

    @BeforeEach
    void setUp() {
        rentalRepository = mock(RentalRepository.class);
        userRepository = mock(UserRepository.class);
        rentalPictureStorageService = mock(RentalPictureStorageService.class);
        rentalService = new RentalService(rentalRepository, userRepository, rentalPictureStorageService);
    }

    @Test
    void findAllReturnsMappedRentals() {
        User owner = new User("owner@example.com", "Owner", "encoded-password");
        ReflectionTestUtils.setField(owner, "id", 2);

        Rental rental = new Rental(
            "House",
            120L,
            950L,
            "http://localhost:9001/api/uploads/rentals/house.jpg",
            "A nice house",
            owner
        );
        ReflectionTestUtils.setField(rental, "id", 1);
        ReflectionTestUtils.setField(rental, "createdAt", LocalDateTime.of(2026, 7, 11, 10, 30));
        ReflectionTestUtils.setField(rental, "updatedAt", LocalDateTime.of(2026, 7, 11, 10, 45));

        when(rentalRepository.findAll()).thenReturn(List.of(rental));

        RentalsResponse response = rentalService.findAll();

        assertThat(response.rentals()).hasSize(1);

        RentalSummaryResponse rentalResponse = response.rentals().get(0);
        assertThat(rentalResponse.id()).isEqualTo(1);
        assertThat(rentalResponse.name()).isEqualTo("House");
        assertThat(rentalResponse.surface()).isEqualTo(120L);
        assertThat(rentalResponse.price()).isEqualTo(950L);
        assertThat(rentalResponse.picture()).isEqualTo("http://localhost:9001/api/uploads/rentals/house.jpg");
        assertThat(rentalResponse.description()).isEqualTo("A nice house");
        assertThat(rentalResponse.ownerId()).isEqualTo(2);
        assertThat(rentalResponse.createdAt()).isEqualTo(LocalDateTime.of(2026, 7, 11, 10, 30));
        assertThat(rentalResponse.updatedAt()).isEqualTo(LocalDateTime.of(2026, 7, 11, 10, 45));
    }

    @Test
    void findByIdReturnsMappedRental() {
        User owner = new User("owner@example.com", "Owner", "encoded-password");
        ReflectionTestUtils.setField(owner, "id", 2);

        Rental rental = new Rental(
            "House",
            120L,
            950L,
            "http://localhost:9001/api/uploads/rentals/house.jpg",
            "A nice house",
            owner
        );
        ReflectionTestUtils.setField(rental, "id", 1);
        ReflectionTestUtils.setField(rental, "createdAt", LocalDateTime.of(2026, 7, 11, 10, 30));
        ReflectionTestUtils.setField(rental, "updatedAt", LocalDateTime.of(2026, 7, 11, 10, 45));

        when(rentalRepository.findById(1)).thenReturn(Optional.of(rental));

        RentalSummaryResponse rentalResponse = rentalService.findById(1);

        assertThat(rentalResponse.id()).isEqualTo(1);
        assertThat(rentalResponse.name()).isEqualTo("House");
        assertThat(rentalResponse.surface()).isEqualTo(120L);
        assertThat(rentalResponse.price()).isEqualTo(950L);
        assertThat(rentalResponse.picture()).isEqualTo("http://localhost:9001/api/uploads/rentals/house.jpg");
        assertThat(rentalResponse.description()).isEqualTo("A nice house");
        assertThat(rentalResponse.ownerId()).isEqualTo(2);
        assertThat(rentalResponse.createdAt()).isEqualTo(LocalDateTime.of(2026, 7, 11, 10, 30));
        assertThat(rentalResponse.updatedAt()).isEqualTo(LocalDateTime.of(2026, 7, 11, 10, 45));
    }

    @Test
    void findByIdThrowsNotFoundWhenRentalDoesNotExist() {
        when(rentalRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.findById(1))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createSavesRentalForAuthenticatedUser() {
        User owner = new User("test@example.com", "Test", "encoded-password");

        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(rentalPictureStorageService.store(any(MultipartFile.class)))
            .thenReturn("http://localhost:9001/api/uploads/rentals/house.jpg");
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalResponse response = rentalService.create(validRequest(), authenticationWithUserId(1));

        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository).save(rentalCaptor.capture());

        Rental savedRental = rentalCaptor.getValue();

        assertThat(response.message()).isEqualTo("Rental created !");
        assertThat(savedRental.getName()).isEqualTo("House");
        assertThat(savedRental.getSurface()).isEqualTo(120L);
        assertThat(savedRental.getPrice()).isEqualTo(950L);
        assertThat(savedRental.getDescription()).isEqualTo("A nice house");
        assertThat(savedRental.getOwner()).isSameAs(owner);
        assertThat(savedRental.getPicture()).isEqualTo("http://localhost:9001/api/uploads/rentals/house.jpg");
    }

    @Test
    void createThrowsBadRequestWhenPictureIsEmpty() {
        CreateRentalRequest request = validRequest();
        request.setPicture(new MockMultipartFile("picture", new byte[0]));

        when(userRepository.findById(1)).thenReturn(Optional.of(new User("test@example.com", "Test", "encoded-password")));
        when(rentalPictureStorageService.store(request.getPicture()))
            .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Picture is required"));

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

    @Test
    void updateChangesRentalWithoutReplacingPicture() {
        Rental rental = rentalWithOwnerId(1);
        UpdateRentalRequest request = validUpdateRequest();

        when(rentalRepository.findById(1)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalResponse response = rentalService.update(1, request, authenticationWithUserId(1));

        assertThat(response.message()).isEqualTo("Rental updated !");
        assertThat(rental.getName()).isEqualTo("Updated house");
        assertThat(rental.getSurface()).isEqualTo(140L);
        assertThat(rental.getPrice()).isEqualTo(980L);
        assertThat(rental.getPicture()).isEqualTo("http://localhost:9001/api/uploads/rentals/house.jpg");
        assertThat(rental.getDescription()).isEqualTo("Updated description");

        verify(rentalRepository).save(rental);
        verify(rentalPictureStorageService, never()).store(any(MultipartFile.class));
        verify(rentalPictureStorageService, never()).delete(any());
    }

    @Test
    void updateReplacesPictureAndDeletesPreviousOne() {
        Rental rental = rentalWithOwnerId(1);
        UpdateRentalRequest request = validUpdateRequest();
        request.setPicture(new MockMultipartFile(
            "picture",
            "new-house.jpg",
            "image/jpeg",
            "new-image-content".getBytes()
        ));

        when(rentalRepository.findById(1)).thenReturn(Optional.of(rental));
        when(rentalPictureStorageService.store(request.getPicture()))
            .thenReturn("http://localhost:9001/api/uploads/rentals/new-house.jpg");
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalResponse response = rentalService.update(1, request, authenticationWithUserId(1));

        assertThat(response.message()).isEqualTo("Rental updated !");
        assertThat(rental.getPicture()).isEqualTo("http://localhost:9001/api/uploads/rentals/new-house.jpg");

        verify(rentalPictureStorageService).store(request.getPicture());
        verify(rentalPictureStorageService).delete("http://localhost:9001/api/uploads/rentals/house.jpg");
        verify(rentalRepository).save(rental);
    }

    @Test
    void updateThrowsForbiddenWhenAuthenticatedUserIsNotTheOwner() {
        when(rentalRepository.findById(1)).thenReturn(Optional.of(rentalWithOwnerId(2)));

        assertThatThrownBy(() -> rentalService.update(1, validUpdateRequest(), authenticationWithUserId(1)))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.FORBIDDEN);

        verify(rentalRepository, never()).save(any(Rental.class));
        verify(rentalPictureStorageService, never()).store(any(MultipartFile.class));
        verify(rentalPictureStorageService, never()).delete(any());
    }

    @Test
    void updateThrowsNotFoundWhenRentalDoesNotExist() {
        when(rentalRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.update(1, validUpdateRequest(), authenticationWithUserId(1)))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.NOT_FOUND);

        verify(rentalRepository, never()).save(any(Rental.class));
    }

    private CreateRentalRequest validRequest() {
        CreateRentalRequest request = new CreateRentalRequest();
        request.setName(" House ");
        request.setSurface(120L);
        request.setPrice(950L);
        request.setPicture(new MockMultipartFile(
            "picture",
            "house.jpg",
            "image/jpeg",
            "image-content".getBytes()
        ));
        request.setDescription(" A nice house ");

        return request;
    }

    private UpdateRentalRequest validUpdateRequest() {
        UpdateRentalRequest request = new UpdateRentalRequest();
        request.setName(" Updated house ");
        request.setSurface(140L);
        request.setPrice(980L);
        request.setDescription(" Updated description ");

        return request;
    }

    private Rental rentalWithOwnerId(Integer ownerId) {
        User owner = new User("owner@example.com", "Owner", "encoded-password");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        return new Rental(
            "House",
            120L,
            950L,
            "http://localhost:9001/api/uploads/rentals/house.jpg",
            "A nice house",
            owner
        );
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
