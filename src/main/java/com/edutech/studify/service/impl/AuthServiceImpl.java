package com.edutech.studify.service.impl;


import com.edutech.studify.dto.request.ChangePasswordRequest;
import com.edutech.studify.dto.request.LoginRequest;
import com.edutech.studify.dto.request.RegisterRequest;
import com.edutech.studify.dto.response.AuthResponse;
import com.edutech.studify.dto.response.RegisterResponse;
import com.edutech.studify.dto.response.UserResponse;
import com.edutech.studify.dto.util.DtoMapper;
import com.edutech.studify.entity.Role;
import com.edutech.studify.entity.User;
import com.edutech.studify.exception.BadRequestException;
import com.edutech.studify.exception.DuplicateResourceException;
import com.edutech.studify.exception.ForbiddenException;
import com.edutech.studify.exception.InvalidCredentialsException;
import com.edutech.studify.repository.UserRepository;
import com.edutech.studify.security.JwtUtils;
import com.edutech.studify.service.AuthService;
import com.edutech.studify.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // Authorization Check: Prevent privilege escalation during registration
        if (request.getRole() == Role.ADMIN || request.getRole() == Role.TEACHER) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthenticated = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);

            if (!isAuthenticated) {
                throw new ForbiddenException("Authentication required to register elevated roles.");
            }

            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new ForbiddenException("Only administrators can register users with ADMIN or TEACHER roles.");
            }
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

        userRepository.save(user);

        return RegisterResponse.builder()
                .message("User registered successfully. Please proceed to login.")
                .userId(user.getId())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtUtils.generateToken(authentication);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .expiresIn(jwtUtils.getExpirationMs())
                    .build();

        } catch (AuthenticationException e) {
            // Specifically catching AuthenticationException to avoid swallowing DB/infrastructure errors
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    @Override
    public User getCurrentUser() {
        // DRY: Delegate to SecurityUtils instead of duplicating logic
        return securityUtils.getCurrentUser();
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile() {
        User user = getCurrentUser();
        // Single Responsibility: Delegate mapping to DtoMapper
        return dtoMapper.toUserResponse(user);
    }
}
