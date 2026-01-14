package com.edutech.studify.controller;

import com.edutech.studify.dto.request.ChangePasswordRequest;
import com.edutech.studify.dto.request.LoginRequest;
import com.edutech.studify.dto.request.RegisterRequest;
import com.edutech.studify.dto.response.ApiResponse;
import com.edutech.studify.dto.response.AuthResponse;
import com.edutech.studify.dto.response.UserResponse;
import com.edutech.studify.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/**
 * You don’t need @PreAuthorize in AuthController because:
 * Authentication endpoints must be public
 * Authentication is enforced globally by the filter chain
 * @PreAuthorize is for business rules, not basic authentication
 * */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management APIs")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Create a new user account with role (ADMIN, TEACHER, STUDENT). Returns JWT token."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Username or email already exists"
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    /**
     * Login user
     */
    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticate user with username and password. Returns JWT token valid for 24 hours."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid username or password"
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get current user profile",
            description = "Get complete profile of authenticated user including role-specific information (student/teacher details)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Token missing or invalid"
            )
    })
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {

        UserResponse userResponse = authService.getUserProfile();
        return ResponseEntity.ok(ApiResponse.success(userResponse, "User retrieved successfully"));
    }

    /**
     * Change password
     */
    @PutMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Change password",
            description = "Change current user's password. Requires current password for verification."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Passwords don't match or invalid format"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Current password is incorrect"
            )
    })
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(request);

        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    /**
     * Logout (client-side token removal)
     * POST /api/auth/logout
     * Note: Since we're using stateless JWT, logout is handled client-side
     * This endpoint is for logging purposes or future token blacklisting
     */
    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Logout user",
            description = "Logout user. Client should remove the JWT token. This endpoint is for logging purposes."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logout successful"
            )
    })
    public ResponseEntity<ApiResponse<String>> logout() {
        // For stateless JWT, the client should remove the token
        // This endpoint can be used for logging or future token blacklisting implementation

        return ResponseEntity.ok(ApiResponse.success(null,
                "Logout successful. Please remove the token from client."));
    }
}