package com.edutech.studify.controller;

import com.edutech.studify.dto.request.CreateTeacherRequest;
import com.edutech.studify.dto.request.UpdateTeacherRequest;
import com.edutech.studify.dto.response.ApiResponse;
import com.edutech.studify.dto.response.PageResponse;
import com.edutech.studify.dto.response.TeacherResponse;
import com.edutech.studify.service.TeacherService;
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
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
@Tag(name = "Teacher Management", description = "APIs for managing teachers")
@SecurityRequirement(name = "bearerAuth")
public class TeacherController {

    private final TeacherService teacherService;

    /**
     * Create a new teacher
     * Only accessible by ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new teacher", description = "Create a new teacher with user account (Admin only)")
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(
            @Valid @RequestBody CreateTeacherRequest request) {

        TeacherResponse teacher = teacherService.createTeacher(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<TeacherResponse>builder()
                        .success(true)
                        .message("Teacher created successfully")
                        .data(teacher)
                        .build());
    }

    /**
     * Get all teachers with pagination
     * Only accessible by ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all teachers", description = "Retrieve all teachers with pagination (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<TeacherResponse>>> getAllTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<TeacherResponse> teachers = teacherService.getAllTeachers(page, size, sortBy, sortDir);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<TeacherResponse>>builder()
                        .success(true)
                        .message("Teachers retrieved successfully")
                        .data(teachers)
                        .build());
    }

    /**
     * Get teacher by ID
     * Accessible by ADMIN, TEACHER (own profile), STUDENT (can view their course teachers)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get teacher by ID", description = "Retrieve teacher details by ID")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherById(@PathVariable Long id) {

        TeacherResponse teacher = teacherService.getTeacherById(id);

        return ResponseEntity.ok(
                ApiResponse.<TeacherResponse>builder()
                        .success(true)
                        .message("Teacher retrieved successfully")
                        .data(teacher)
                        .build());
    }

    /**
     * Get teacher by User ID
     * Accessible by the teacher themselves and ADMIN
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get teacher by user ID", description = "Retrieve teacher details by user ID")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherByUserId(@PathVariable Long userId) {

        TeacherResponse teacher = teacherService.getTeacherByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.<TeacherResponse>builder()
                        .success(true)
                        .message("Teacher retrieved successfully")
                        .data(teacher)
                        .build());
    }

    /**
     * Search teachers
     * Accessible by ADMIN
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search teachers", description = "Search teachers by name or email")
    public ResponseEntity<ApiResponse<PageResponse<TeacherResponse>>> searchTeachers(
            @RequestParam String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<TeacherResponse> teachers = teacherService.searchTeachers(
                search, departmentId, page, size, sortBy, sortDir);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<TeacherResponse>>builder()
                        .success(true)
                        .message("Search results retrieved successfully")
                        .data(teachers)
                        .build());
    }

    /**
     * Get teachers by department
     * Accessible by ADMIN
     */
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get teachers by department", description = "Retrieve all teachers in a department")
    public ResponseEntity<ApiResponse<PageResponse<TeacherResponse>>> getTeachersByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PageResponse<TeacherResponse> teachers = teacherService.getTeachersByDepartment(
                departmentId, page, size, sortBy, sortDir);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<TeacherResponse>>builder()
                        .success(true)
                        .message("Teachers retrieved successfully")
                        .data(teachers)
                        .build());
    }

    /**
     * Update teacher
     * Accessible by ADMIN (full update) and TEACHER (own profile - limited fields)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Update teacher", description = "Update teacher information")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTeacherRequest request) {

        TeacherResponse teacher = teacherService.updateTeacher(id, request);

        return ResponseEntity.ok(
                ApiResponse.<TeacherResponse>builder()
                        .success(true)
                        .message("Teacher updated successfully")
                        .data(teacher)
                        .build());
    }

    /**
     * Delete teacher (soft delete)
     * Only accessible by ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete teacher", description = "Soft delete a teacher (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable Long id) {

        teacherService.deleteTeacher(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Teacher deleted successfully")
                        .build());
    }

    /**
     * Restore deleted teacher
     * Only accessible by ADMIN
     */
    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restore deleted teacher", description = "Restore a soft-deleted teacher (Admin only)")
    public ResponseEntity<ApiResponse<TeacherResponse>> restoreTeacher(@PathVariable Long id) {

        TeacherResponse teacher = teacherService.restoreTeacher(id);

        return ResponseEntity.ok(
                ApiResponse.<TeacherResponse>builder()
                        .success(true)
                        .message("Teacher restored successfully")
                        .data(teacher)
                        .build());
    }

    /**
     * Get deleted teachers
     * Only accessible by ADMIN
     */
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get deleted teachers", description = "Retrieve all soft-deleted teachers (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<TeacherResponse>>> getDeletedTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<TeacherResponse> teachers = teacherService.getDeletedTeachers(page, size);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<TeacherResponse>>builder()
                        .success(true)
                        .message("Deleted teachers retrieved successfully")
                        .data(teachers)
                        .build());
    }
}