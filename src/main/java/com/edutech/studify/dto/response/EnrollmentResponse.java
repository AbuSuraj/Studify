package com.edutech.studify.dto.response;

import com.edutech.studify.entity.EnrollmentStatus;
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
public class EnrollmentResponse {

    private Long id;
    private StudentSummary student;
    private CourseSummary course;
    private LocalDate enrollmentDate;
    private EnrollmentStatus status;
    private String currentGrade;
    private Double attendancePercentage;
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
        private Integer credits;
        private String teacherName;
    }
}
