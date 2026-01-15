package com.edutech.studify.controller;

import com.edutech.studify.dto.request.CreateStudentRequest;
import com.edutech.studify.dto.request.UpdateStudentRequest;
import com.edutech.studify.dto.response.ApiResponse;
import com.edutech.studify.dto.response.PageResponse;
import com.edutech.studify.dto.response.StudentResponse;
import com.edutech.studify.entity.StudentStatus;
import com.edutech.studify.service.StudentService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "APIs for managing students")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;

    /**
     * Create a new student
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create new student",
            description = "Create a new student with auto-generated username and default password 'Student@123'. Only ADMIN can perform this action."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Student created successfully",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only ADMIN can create students"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email already exists"
            )
    })
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @Valid @RequestBody CreateStudentRequest request) {
        /**@Valid TRIGGERS:
        - @NotBlank checks
        - @Email format check
        - @Pattern regex validation
        - All validation annotations in DTO*/
        StudentResponse response = studentService.createStudent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Student created successfully"));
    }

    /**
     * Get all students with pagination and filters
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(
            summary = "Get all students",
            description = "Get paginated list of students with optional filters (search, department, status). Accessible by ADMIN and TEACHER."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Students retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions"
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<StudentResponse>>> getAllStudents(
            @Parameter(description = "Search by name or email (case-insensitive)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by department ID")
            @RequestParam(required = false) Long departmentId,

            @Parameter(description = "Filter by status (ACTIVE, INACTIVE, GRADUATED)")
            @RequestParam(required = false) StudentStatus status,

            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {

        PageResponse<StudentResponse> response = studentService.getAllStudents(
                search, departmentId, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(response, "Students retrieved successfully"));
    }

    /**
     * Get student by ID
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get student by ID",
            description = "Get detailed information about a specific student. ADMIN and TEACHER can view any student, STUDENT can only view their own profile."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Student retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Cannot access other student's profile"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Student not found"
            )
    })
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(
            @Parameter(description = "Student ID")
            @PathVariable Long id) {

        StudentResponse response = studentService.getStudentById(id);

        return ResponseEntity.ok(ApiResponse.success(response, "Student retrieved successfully"));
    }

    /**
     * Get current student profile

     *         /*
     *         BUSINESS SCENARIO:
     *         Student logs in → wants to see their own profile
     *         Don't need to know their student ID
     *         Just call /api/v1/students/me

     *         FLOW:
     *         1. JWT contains user email
     *         2. Security loads user from database
     *         3. Service finds student by user ID
     *         4. Returns student profile

     *         WHY SEPARATE ENDPOINT:
     *         - Convenience (don't need ID)
     *         - Security (can't accidentally view wrong student)
     *         - Common pattern in REST APIs
     *         */

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
            summary = "Get my profile",
            description = "Get complete profile of currently logged-in student"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Student profile not found"
            )
    })
    public ResponseEntity<ApiResponse<StudentResponse>> getMyProfile() {

        StudentResponse response = studentService.getMyProfile();

        return ResponseEntity.ok(ApiResponse.success(response, "Profile retrieved successfully"));
    }

    /**
     * Update student
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update student",
            description = "Update student information. ADMIN can update all fields, STUDENT can only update phone and address."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Student updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Cannot update other student's profile"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Student not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email already exists"
            )
    })
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @Parameter(description = "Student ID")
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentRequest request) {

        StudentResponse response = studentService.updateStudent(id, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Student updated successfully"));
    }

    /**
     * Delete student (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete student",
            description = "Soft delete a student (can be restored later). Deactivates user account. Only ADMIN can perform this action."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Student deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only ADMIN can delete students"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Student not found"
            )
    })
    public ResponseEntity<ApiResponse<String>> deleteStudent(
            @Parameter(description = "Student ID") @PathVariable Long id) {

        studentService.deleteStudent(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Student deleted successfully"));
    }

    /**
     * Restore deleted student
     */
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Restore deleted student",
            description = "Restore a soft-deleted student. Reactivates user account. Only ADMIN can perform this action."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Student restored successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only ADMIN can restore students"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Student not found"
            )
    })
    public ResponseEntity<ApiResponse<StudentResponse>> restoreStudent(
            @Parameter(description = "Student ID") @PathVariable Long id) {

        StudentResponse response = studentService.restoreStudent(id);

        return ResponseEntity.ok(ApiResponse.success(response, "Student restored successfully"));
    }
}
