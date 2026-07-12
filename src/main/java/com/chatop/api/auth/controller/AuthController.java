package com.chatop.api.auth.controller;

import static com.chatop.api.config.OpenApiConfig.COOKIE_AUTH_SECURITY_SCHEME;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatop.api.auth.dto.AuthResponse;
import com.chatop.api.auth.dto.AuthenticatedUserResponse;
import com.chatop.api.auth.dto.LoginRequest;
import com.chatop.api.auth.dto.RegisterRequest;
import com.chatop.api.auth.security.JwtCookieService;
import com.chatop.api.auth.service.AuthService;
import com.chatop.api.config.properties.ResponseMessagesProperties;
import com.chatop.api.exception.dto.ApiErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes authentication endpoints used by the ChaTop client.
 */
@Tag(name = "Authentication", description = "Registration, login and session endpoints")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtCookieService jwtCookieService;
    private final ResponseMessagesProperties responses;

    public AuthController(
        AuthService authService,
        JwtCookieService jwtCookieService,
        ResponseMessagesProperties responses
    ) {
        this.authService = authService;
        this.jwtCookieService = jwtCookieService;
        this.responses = responses;
    }

    /**
     * Registers a new user account and starts an authenticated session.
     *
     * @param request the registration payload containing the user's name, email and password
     * @return a success response and an authentication cookie
     */
    @Operation(summary = "Register a user", description = "Creates a user account and starts an authenticated session.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User registered",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error or email already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return authenticatedResponse(authService.register(request));
    }

    /**
     * Authenticates an existing user and starts an authenticated session.
     *
     * @param request the login payload containing the user's email and password
     * @return a success response and an authentication cookie
     */
    @Operation(summary = "Log in a user", description = "Authenticates a user and starts an authenticated session.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User authenticated",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return authenticatedResponse(authService.login(request));
    }

    /**
     * Returns the currently authenticated user's public profile.
     *
     * @param authentication the authenticated principal resolved by Spring Security
     * @return the authenticated user's public information
     */
    @Operation(
        summary = "Get authenticated user",
        description = "Returns the public profile of the currently authenticated user.",
        security = @SecurityRequirement(name = COOKIE_AUTH_SECURITY_SCHEME)
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Authenticated user profile",
            content = @Content(schema = @Schema(implementation = AuthenticatedUserResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication is required",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    @GetMapping("/me")
    public AuthenticatedUserResponse me(@Parameter(hidden = true) Authentication authentication) {
        return authService.me(authentication);
    }

    /**
     * Initializes the CSRF token for browser clients.
     *
     * @param csrfToken the CSRF token resolved by Spring Security
     * @return an empty response once the CSRF token has been resolved
     */
    @Operation(summary = "Initialize CSRF token", description = "Creates the CSRF cookie required by browser clients.")
    @ApiResponse(responseCode = "204", description = "CSRF token initialized")
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(@Parameter(hidden = true) CsrfToken csrfToken) {
        csrfToken.getToken();

        return ResponseEntity.noContent().build();
    }

    /**
     * Clears the authentication cookie for the current client.
     *
     * @return a success response and an expired authentication cookie
     */
    @Operation(
        summary = "Log out a user",
        description = "Clears the authentication cookie for the current client.",
        security = @SecurityRequirement(name = COOKIE_AUTH_SECURITY_SCHEME)
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User logged out",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication is required",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "CSRF token is missing or invalid",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookieService.clearAuthenticationCookie().toString())
            .body(new AuthResponse(responses.getLogoutSuccessful()));
    }

    private ResponseEntity<AuthResponse> authenticatedResponse(String token) {
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookieService.createAuthenticationCookie(token).toString())
            .body(new AuthResponse(responses.getAuthenticationSuccessful()));
    }
}
