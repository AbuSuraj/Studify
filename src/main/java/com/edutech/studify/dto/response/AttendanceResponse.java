package com.edutech.studify.dto.response;

import com.edutech.studify.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {

    private Long id;
    private Long enrollmentId;
    private StudentSummary student;
    private CourseSummary course;
    private LocalDate date;
    private AttendanceStatus status;
    private LocalDateTime createdAt;
    private String createdBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentSummary {
        private Long id;
        private String fullName;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseSummary {
        private Long id;
        private String courseCode;
        private String name;
    }
}
