package com.edutech.studify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranscriptResponse {

    private Long studentId;
    private String studentName;
    private String department;
    private LocalDate enrollmentDate;
    private List<SemesterGrades> semesters;
    private BigDecimal cumulativeGPA;
    private Integer totalCreditsEarned;
    private LocalDateTime generatedDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SemesterGrades {
        private String semester;
        private List<CourseGrade> courses;
        private BigDecimal semesterGPA;
        private Integer totalCredits;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseGrade {
        private String courseCode;
        private String courseName;
        private Integer credits;
        private String grade;
        private BigDecimal gradePoint;
    }
}
