package com.edutech.studify.controller;

import com.edutech.studify.dto.request.EnrollmentRequest;
import com.edutech.studify.dto.response.ApiResponse;
import com.edutech.studify.dto.response.EnrollmentResponse;
import com.edutech.studify.dto.response.PageResponse;
import com.edutech.studify.entity.EnrollmentStatus;
import com.edutech.studify.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollment Management", description = "APIs for managing course enrollments")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Enroll student in course
     * ADMIN can enroll any student, STUDENT can self-enroll
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    @Operation(summary = "Enroll student in course", description = "Enroll a student in a course")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollStudent(
            @Valid @RequestBody EnrollmentRequest request) {

        EnrollmentResponse enrollment = enrollmentService.enrollStudent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<EnrollmentResponse>builder()
                        .success(true)
                        .message("Student enrolled successfully")
                        .data(enrollment)
                        .build());
    }

    /**
     * Drop course (unenroll)
     * ADMIN can drop any enrollment, STUDENT can drop own
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    @Operation(summary = "Drop course", description = "Unenroll student from course")
    public ResponseEntity<ApiResponse<Void>> dropCourse(@PathVariable Long id) {

        enrollmentService.dropCourse(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Course dropped successfully")
                        .build());
    }

    /**
     * Alternative endpoint to drop course
     */
    @PutMapping("/{id}/drop")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    @Operation(summary = "Drop course (alternative)", description = "Unenroll student from course")
    public ResponseEntity<ApiResponse<Void>> dropCourseAlt(@PathVariable Long id) {

        enrollmentService.dropCourse(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Course dropped successfully")
                        .build());
    }

    /**
     * Get enrollment by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get enrollment by ID", description = "Retrieve enrollment details by ID")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentById(@PathVariable Long id) {

        EnrollmentResponse enrollment = enrollmentService.getEnrollmentById(id);

        return ResponseEntity.ok(
                ApiResponse.<EnrollmentResponse>builder()
                        .success(true)
                        .message("Enrollment retrieved successfully")
                        .data(enrollment)
                        .build());
    }

    /**
     * Get student enrollments
     * ADMIN and TEACHER can view all, STUDENT can view own
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    @Operation(summary = "Get student enrollments", description = "Retrieve all enrollments for a student")
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentResponse>>> getStudentEnrollments(
            @PathVariable Long studentId,
            @RequestParam(required = false) EnrollmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<EnrollmentResponse> enrollments = enrollmentService.getStudentEnrollments(
                studentId, status, page, size);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<EnrollmentResponse>>builder()
                        .success(true)
                        .message("Enrollments retrieved successfully")
                        .data(enrollments)
                        .build());
    }

    /**
     * Get course enrollments
     * ADMIN and TEACHER (own courses) can view
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Get course enrollments", description = "Retrieve all enrollments for a course")
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentResponse>>> getCourseEnrollments(
            @PathVariable Long courseId,
            @RequestParam(required = false) EnrollmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<EnrollmentResponse> enrollments = enrollmentService.getCourseEnrollments(
                courseId, status, page, size);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<EnrollmentResponse>>builder()
                        .success(true)
                        .message("Enrollments retrieved successfully")
                        .data(enrollments)
                        .build());
    }

    /**
     * Get active enrollments for student
     */
    @GetMapping("/student/{studentId}/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    @Operation(summary = "Get active student enrollments", description = "Retrieve active enrollments for a student")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getActiveStudentEnrollments(
            @PathVariable Long studentId) {

        List<EnrollmentResponse> enrollments = enrollmentService.getActiveEnrollmentsByStudentId(studentId);

        return ResponseEntity.ok(
                ApiResponse.<List<EnrollmentResponse>>builder()
                        .success(true)
                        .message("Active enrollments retrieved successfully")
                        .data(enrollments)
                        .build());
    }

    /**
     * Get active enrollments for course
     */
    @GetMapping("/course/{courseId}/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Get active course enrollments", description = "Retrieve active enrollments for a course")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getActiveCourseEnrollments(
            @PathVariable Long courseId) {

        List<EnrollmentResponse> enrollments = enrollmentService.getActiveEnrollmentsByCourseId(courseId);

        return ResponseEntity.ok(
                ApiResponse.<List<EnrollmentResponse>>builder()
                        .success(true)
                        .message("Active enrollments retrieved successfully")
                        .data(enrollments)
                        .build());
    }
}