package com.edutech.studify.controller;

import com.edutech.studify.dto.request.AttendanceRequest;
import com.edutech.studify.dto.response.ApiResponse;
import com.edutech.studify.dto.response.AttendanceResponse;
import com.edutech.studify.dto.response.AttendanceSummaryResponse;
import com.edutech.studify.entity.AttendanceStatus;
import com.edutech.studify.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance Management", description = "APIs for managing student attendance")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Mark attendance for multiple students
     * TEACHER can mark for their courses
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Mark attendance", description = "Mark attendance for students in a course")
    public ResponseEntity<ApiResponse<AttendanceSummaryResponse>> markAttendance(
            @Valid @RequestBody AttendanceRequest request) {

        AttendanceSummaryResponse summary = attendanceService.markAttendance(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<AttendanceSummaryResponse>builder()
                        .success(true)
                        .message("Attendance marked successfully")
                        .data(summary)
                        .build());
    }

    /**
     * Update single attendance record
     * TEACHER who created it
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Update attendance", description = "Update attendance status")
    public ResponseEntity<ApiResponse<AttendanceResponse>> updateAttendance(
            @PathVariable Long id,
            @RequestParam AttendanceStatus status) {

        AttendanceResponse attendance = attendanceService.updateAttendance(id, status);

        return ResponseEntity.ok(
                ApiResponse.<AttendanceResponse>builder()
                        .success(true)
                        .message("Attendance updated successfully")
                        .data(attendance)
                        .build());
    }

    /**
     * Get attendance by student
     * ADMIN, TEACHER (their courses), STUDENT (own)
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    @Operation(summary = "Get student attendance", description = "Retrieve attendance records for a student")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getStudentAttendance(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<AttendanceResponse> attendance = attendanceService.getAttendanceByStudent(
                studentId, courseId, startDate, endDate);

        return ResponseEntity.ok(
                ApiResponse.<List<AttendanceResponse>>builder()
                        .success(true)
                        .message("Attendance retrieved successfully")
                        .data(attendance)
                        .build());
    }

    /**
     * Get attendance by course
     * ADMIN, TEACHER (own courses)
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Get course attendance", description = "Retrieve attendance records for a course")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getCourseAttendance(
            @PathVariable Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AttendanceResponse> attendance = attendanceService.getAttendanceByCourse(courseId, date);

        return ResponseEntity.ok(
                ApiResponse.<List<AttendanceResponse>>builder()
                        .success(true)
                        .message("Attendance retrieved successfully")
                        .data(attendance)
                        .build());
    }

    /**
     * Get attendance statistics for course
     * ADMIN, TEACHER (own courses)
     */
    @GetMapping("/course/{courseId}/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Get attendance statistics", description = "Get attendance statistics for a course")
    public ResponseEntity<ApiResponse<AttendanceSummaryResponse>> getAttendanceStatistics(
            @PathVariable Long courseId) {

        AttendanceSummaryResponse summary = attendanceService.getAttendanceStatistics(courseId);

        return ResponseEntity.ok(
                ApiResponse.<AttendanceSummaryResponse>builder()
                        .success(true)
                        .message("Statistics retrieved successfully")
                        .data(summary)
                        .build());
    }
}