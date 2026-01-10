package com.edutech.studify.controller;

import com.edutech.studify.dto.request.CreateCourseRequest;
import com.edutech.studify.dto.request.UpdateCourseRequest;
import com.edutech.studify.dto.response.ApiResponse;
import com.edutech.studify.dto.response.CourseResponse;
import com.edutech.studify.dto.response.PageResponse;
import com.edutech.studify.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Course Management", description = "APIs for managing courses")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {

    private final CourseService courseService;

    /**
     * Create a new course
     * Only accessible by ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new course", description = "Create a new course (Admin only)")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CreateCourseRequest request) {

        CourseResponse course = courseService.createCourse(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<CourseResponse>builder()
                        .success(true)
                        .message("Course created successfully")
                        .data(course)
                        .build());
    }

    /**
     * Get all courses with pagination and filters
     * Accessible by all authenticated users
     */
    @GetMapping
    @Operation(summary = "Get all courses", description = "Retrieve all courses with pagination and filters")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getAllCourses(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<CourseResponse> courses = courseService.getAllCourses(
                departmentId, semester, teacherId, page, size, sortBy, sortDir);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<CourseResponse>>builder()
                        .success(true)
                        .message("Courses retrieved successfully")
                        .data(courses)
                        .build());
    }

    /**
     * Search courses by name or code
     * Accessible by all authenticated users
     */
    @GetMapping("/search")
    @Operation(summary = "Search courses", description = "Search courses by name or code")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> searchCourses(
            @RequestParam String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<CourseResponse> courses = courseService.searchCourses(
                search, departmentId, semester, teacherId, page, size, sortBy, sortDir);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<CourseResponse>>builder()
                        .success(true)
                        .message("Search results retrieved successfully")
                        .data(courses)
                        .build());
    }

    /**
     * Get course by ID
     * Accessible by all authenticated users
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID", description = "Retrieve course details by ID")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long id) {

        CourseResponse course = courseService.getCourseById(id);

        return ResponseEntity.ok(
                ApiResponse.<CourseResponse>builder()
                        .success(true)
                        .message("Course retrieved successfully")
                        .data(course)
                        .build());
    }

    /**
     * Get course by course code
     * Accessible by all authenticated users
     */
    @GetMapping("/code/{courseCode}")
    @Operation(summary = "Get course by code", description = "Retrieve course details by course code")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseByCourseCode(@PathVariable String courseCode) {

        CourseResponse course = courseService.getCourseByCourseCode(courseCode);

        return ResponseEntity.ok(
                ApiResponse.<CourseResponse>builder()
                        .success(true)
                        .message("Course retrieved successfully")
                        .data(course)
                        .build());
    }

    /**
     * Get available courses (not full)
     * Accessible by all authenticated users
     */
    @GetMapping("/available")
    @Operation(summary = "Get available courses", description = "Retrieve courses with available seats")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getAvailableCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<CourseResponse> courses = courseService.getAvailableCourses(page, size);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<CourseResponse>>builder()
                        .success(true)
                        .message("Available courses retrieved successfully")
                        .data(courses)
                        .build());
    }

    /**
     * Update course
     * Only accessible by ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update course", description = "Update course information (Admin only)")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {

        CourseResponse course = courseService.updateCourse(id, request);

        return ResponseEntity.ok(
                ApiResponse.<CourseResponse>builder()
                        .success(true)
                        .message("Course updated successfully")
                        .data(course)
                        .build());
    }

    /**
     * Assign or change teacher for a course
     * Only accessible by ADMIN
     */
    @PutMapping("/{courseId}/assign-teacher")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign teacher to course", description = "Assign or change teacher for a course (Admin only)")
    public ResponseEntity<ApiResponse<CourseResponse>> assignTeacher(
            @PathVariable Long courseId,
            @RequestParam Long teacherId) {

        CourseResponse course = courseService.assignTeacher(courseId, teacherId);

        return ResponseEntity.ok(
                ApiResponse.<CourseResponse>builder()
                        .success(true)
                        .message("Teacher assigned successfully")
                        .data(course)
                        .build());
    }

    /**
     * Delete course
     * Only accessible by ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete course", description = "Delete a course (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {

        courseService.deleteCourse(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Course deleted successfully")
                        .build());
    }
}
