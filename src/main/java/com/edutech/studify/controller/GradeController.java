package com.edutech.studify.controller;

import com.edutech.studify.dto.request.GradeRequest;
import com.edutech.studify.dto.response.ApiResponse;
import com.edutech.studify.dto.response.GradeResponse;
import com.edutech.studify.dto.response.PageResponse;
import com.edutech.studify.service.GradeService;
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
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
@Tag(name = "Grade Management", description = "APIs for managing student grades")
@SecurityRequirement(name = "bearerAuth")
public class GradeController {

    private final GradeService gradeService;

    /**
     * Add or update grade
     * TEACHER can grade students in their courses
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Add or update grade", description = "Add or update grade for a student enrollment")
    public ResponseEntity<ApiResponse<GradeResponse>> addOrUpdateGrade(
            @Valid @RequestBody GradeRequest request) {

        GradeResponse grade = gradeService.addOrUpdateGrade(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<GradeResponse>builder()
                        .success(true)
                        .message("Grade saved successfully")
                        .data(grade)
                        .build());
    }

    /**
     * Alternative endpoint for updating grade
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Update grade", description = "Update an existing grade")
    public ResponseEntity<ApiResponse<GradeResponse>> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody GradeRequest request) {

        GradeResponse grade = gradeService.addOrUpdateGrade(request);

        return ResponseEntity.ok(
                ApiResponse.<GradeResponse>builder()
                        .success(true)
                        .message("Grade updated successfully")
                        .data(grade)
                        .build());
    }

    /**
     * Get grade by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get grade by ID", description = "Retrieve grade details by ID")
    public ResponseEntity<ApiResponse<GradeResponse>> getGradeById(@PathVariable Long id) {

        GradeResponse grade = gradeService.getGradeById(id);

        return ResponseEntity.ok(
                ApiResponse.<GradeResponse>builder()
                        .success(true)
                        .message("Grade retrieved successfully")
                        .data(grade)
                        .build());
    }

    /**
     * Get grades by student
     * ADMIN, TEACHER (their courses), STUDENT (own)
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    @Operation(summary = "Get student grades", description = "Retrieve grades for a student")
    public ResponseEntity<ApiResponse<PageResponse<GradeResponse>>> getStudentGrades(
            @PathVariable Long studentId,
            @RequestParam(required = false) String semester,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<GradeResponse> grades = gradeService.getGradesByStudent(
                studentId, semester, page, size);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<GradeResponse>>builder()
                        .success(true)
                        .message("Grades retrieved successfully")
                        .data(grades)
                        .build());
    }

    /**
     * Get all grades by student (no pagination)
     */
    @GetMapping("/student/{studentId}/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    @Operation(summary = "Get all student grades", description = "Retrieve all grades for a student")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getAllStudentGrades(
            @PathVariable Long studentId,
            @RequestParam(required = false) String semester) {

        List<GradeResponse> grades = gradeService.getAllGradesByStudent(studentId, semester);

        return ResponseEntity.ok(
                ApiResponse.<List<GradeResponse>>builder()
                        .success(true)
                        .message("Grades retrieved successfully")
                        .data(grades)
                        .build());
    }

    /**
     * Get grades by course
     * ADMIN and TEACHER (own courses)
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Get course grades", description = "Retrieve grades for a course")
    public ResponseEntity<ApiResponse<PageResponse<GradeResponse>>> getCourseGrades(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<GradeResponse> grades = gradeService.getGradesByCourse(courseId, page, size);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<GradeResponse>>builder()
                        .success(true)
                        .message("Grades retrieved successfully")
                        .data(grades)
                        .build());
    }

    /**
     * Delete grade
     * ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete grade", description = "Delete a grade (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteGrade(@PathVariable Long id) {

        gradeService.deleteGrade(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Grade deleted successfully")
                        .build());
    }
}