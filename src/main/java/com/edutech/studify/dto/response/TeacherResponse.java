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
public class TeacherResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private DepartmentSummary department;
    private String specialization;
    private Integer totalCourses;
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
}