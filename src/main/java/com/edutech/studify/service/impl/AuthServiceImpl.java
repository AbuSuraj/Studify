package com.edutech.studify.service.impl;

import com.edutech.studify.dto.request.ChangePasswordRequest;
import com.edutech.studify.dto.request.LoginRequest;
import com.edutech.studify.dto.request.RefreshTokenRequest;
import com.edutech.studify.dto.request.RegisterRequest;
import com.edutech.studify.dto.response.AuthResponse;
import com.edutech.studify.dto.response.RegisterResponse;
import com.edutech.studify.dto.response.UserResponse;
import com.edutech.studify.dto.util.DtoMapper;
import com.edutech.studify.entity.Role;
import com.edutech.studify.entity.User;
import com.edutech.studify.exception.*;
import com.edutech.studify.repository.UserRepository;
import com.edutech.studify.security.JwtUtils;
import com.edutech.studify.service.AuthService;
import com.edutech.studify.service.RefreshTokenService;
import com.edutech.studify.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SecurityUtils securityUtils;
    private final DtoMapper dtoMapper;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if ((request.getRole() == Role.ADMIN || request.getRole() == Role.TEACHER)
                && !securityUtils.isCurrentUserAdmin()) {
            throw new ForbiddenException("Only administrators can register users with ADMIN or TEACHER roles.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {} with role {}", savedUser.getEmail(), savedUser.getRole());

        return dtoMapper.toRegisterResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtUtils.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        String refreshToken = refreshTokenService.createRefreshToken(user, request.isTerminateOtherSessions());

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtUtils.getExpirationMs())
                .build();
    }

    @Override
    @Transactional(noRollbackFor = TokenRefreshException.class)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        User user = refreshTokenService.validateAndConsume(request.getRefreshToken());

        String newAccessToken = jwtUtils.generateTokenFromUsername(user.getEmail());
        String newRefreshToken = refreshTokenService.createRefreshToken(user,false);

        log.info("Access token refreshed for user: {}", user.getEmail());

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtUtils.getExpirationMs())
                .build();
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revokeToken(request.getRefreshToken());
        log.info("Refresh token revoked (logout).");
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        User user = securityUtils.getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // A refresh token issued before this change shouldn't survive it.
        refreshTokenService.revokeAllUserTokens(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile() {
        return dtoMapper.toUserResponse(securityUtils.getCurrentUser());
    }
}