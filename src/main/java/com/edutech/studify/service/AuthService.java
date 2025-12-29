package com.edutech.studify.service;


import com.edutech.studify.dto.request.ChangePasswordRequest;
import com.edutech.studify.dto.request.LoginRequest;
import com.edutech.studify.dto.request.RegisterRequest;
import com.edutech.studify.dto.response.AuthResponse;
import com.edutech.studify.dto.response.UserResponse;
import com.edutech.studify.entity.Role;
import com.edutech.studify.entity.User;
import com.edutech.studify.exception.BadRequestException;
import com.edutech.studify.exception.DuplicateResourceException;
import com.edutech.studify.exception.InvalidCredentialsException;
import com.edutech.studify.repository.UserRepository;
import com.edutech.studify.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();

        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtils.generateTokenFromUsername(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtUtils.getExpirationMs())
                .build();
    }

    /**
     * Login user
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String token = jwtUtils.generateToken(authentication);

            // Get user details
            User user = userRepository.findByEmail(request.getPassword())
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

        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(Role role) {
        User currentUser = getCurrentUser();
        return currentUser.getRole() == role;
    }

    /**
     * Change password
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Get current user
        User user = getCurrentUser();

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Get user profile with complete information
     */
    @Transactional(readOnly = true)
    public UserResponse getUserProfile() {
        User user = getCurrentUser();

        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt()) ;

        // Add student info if user is a student
        if (user.getStudent() != null) {
            builder.studentInfo(UserResponse.StudentInfo.builder()
                    .studentId(user.getStudent().getId())
                    .fullName(user.getStudent().getFullName())
                    .department(user.getStudent().getDepartment() != null ?
                            user.getStudent().getDepartment().getName() : null)
                    .status(user.getStudent().getStatus().name())
                    .build());
        }

        // Add teacher info if user is a teacher
        if (user.getTeacher() != null) {
            builder.teacherInfo(UserResponse.TeacherInfo.builder()
                    .teacherId(user.getTeacher().getId())
                    .fullName(user.getTeacher().getFullName())
                    .department(user.getTeacher().getDepartment() != null ?
                            user.getTeacher().getDepartment().getName() : null)
                    .specialization(user.getTeacher().getSpecialization())
                    .build());
        }

        return builder.build();
    }
}
