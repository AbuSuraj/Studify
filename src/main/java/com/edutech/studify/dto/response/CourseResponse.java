package com.edutech.studify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {

    private Long id;
    private String courseCode;
    private String name;
    private String description;
    private Integer credits;
    private String semester;
    private Integer maxCapacity;
    private Integer enrolledCount;
    private Integer availableSeats;
    private Boolean isFull;
    private DepartmentSummary department;
    private TeacherSummary teacher;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentSummary {
        private Long id;
        private String name;
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherSummary {
        private Long id;
        private String fullName;
        private String email;
    }
}