package com.edutech.studify.dto.request;

import com.edutech.studify.entity.AttendanceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotEmpty(message = "Attendance records are required")
    @Valid
    private List<AttendanceRecord> attendanceRecords;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceRecord {

        @NotNull(message = "Enrollment ID is required")
        private Long enrollmentId;

        @NotNull(message = "Status is required")
        private AttendanceStatus status;
    }
}