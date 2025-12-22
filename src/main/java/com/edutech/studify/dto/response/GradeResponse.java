package com.edutech.studify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeResponse {

    private Long id;
    private Long enrollmentId;
    private StudentSummary student;
    private CourseSummary course;
    private String grade;
    private BigDecimal gradePoint;
    private String remarks;
    private LocalDate gradedDate;
    private String gradedBy;
    private LocalDateTime createdAt;

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
    }
}