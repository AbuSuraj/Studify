package com.edutech.studify.util;

import com.edutech.studify.entity.Role;
import com.edutech.studify.entity.User;
import com.edutech.studify.exception.ForbiddenException;
import com.edutech.studify.exception.UnauthorizedException;
import com.edutech.studify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for security-related operations
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user found");
        }

        String username = authentication.getName();

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    /**
     * Get current user ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Get current username
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    /**
     * Get current user role
     */
    public Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(Role role) {
        return getCurrentUserRole() == role;
    }

    /**
     * Check if current user is ADMIN
     */
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    /**
     * Check if current user is TEACHER
     */
    public boolean isTeacher() {
        return hasRole(Role.TEACHER);
    }

    /**
     * Check if current user is STUDENT
     */
    public boolean isStudent() {
        return hasRole(Role.STUDENT);
    }

    /**
     * Verify user has permission to access resource
     * Students can only access their own resources
     */
    public void verifyStudentAccess(Long studentUserId) {
        User currentUser = getCurrentUser();

        // Admin and Teacher can access any student
        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.TEACHER) {
            return;
        }

        // Student can only access their own data
        if (currentUser.getRole() == Role.STUDENT) {
            if (!currentUser.getId().equals(studentUserId)) {
                throw new ForbiddenException("You can only access your own data");
            }
        }
    }

    /**
     * Verify user has permission to modify resource
     * Only ADMIN can modify most resources
     */
    public void verifyAdminAccess() {
        if (!isAdmin()) {
            throw new ForbiddenException("Only administrators can perform this action");
        }
    }

    /**
     * Verify teacher has permission to access their courses
     */
    public void verifyTeacherCourseAccess(Long teacherId) {
        User currentUser = getCurrentUser();

        // Admin can access any teacher's courses
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        // Teacher can only access their own courses
        if (currentUser.getRole() == Role.TEACHER) {
            // Compare teacher entity ID with current user's teacher ID
            // This will be implemented when we have Teacher service
            Long currentTeacherId = getCurrentUser().getTeacher().getId();
            if (!currentTeacherId.equals(teacherId)) {
                throw new ForbiddenException("You can only access your own courses");
            }
        } else {
            throw new ForbiddenException("Only teachers can access course details");
        }
    }
}