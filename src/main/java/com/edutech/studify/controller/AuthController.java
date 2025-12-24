package com.edutech.studify.controller;

import com.edutech.studify.dto.request.ChangePasswordRequest;
import com.edutech.studify.dto.request.LoginRequest;
import com.edutech.studify.dto.request.RegisterRequest;
import com.edutech.studify.dto.response.ApiResponse;
import com.edutech.studify.dto.response.AuthResponse;
import com.edutech.studify.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    /**
     * Get current user info
     * GET /api/auth/me
     */
    @GetMapping("/me")

    public ResponseEntity<ApiResponse<Object>> getCurrentUser() {
        var user = authService.getCurrentUser();

        var userInfo = new Object() {
            public final Long id = user.getId();
            public final String username = user.getUsername();
            public final String email = user.getEmail();
            public final String role = user.getRole().name();
        };

        return ResponseEntity.ok(ApiResponse.success(userInfo, "User retrieved successfully"));
    }

    /**
     * Change password
     * PUT /api/auth/change-password
     */
    @PutMapping("/change-password")
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
    public ResponseEntity<ApiResponse<String>> logout() {
        // For stateless JWT, the client should remove the token
        // This endpoint can be used for logging or future token blacklisting implementation

        return ResponseEntity.ok(ApiResponse.success(null,
                "Logout successful. Please remove the token from client."));
    }
}
