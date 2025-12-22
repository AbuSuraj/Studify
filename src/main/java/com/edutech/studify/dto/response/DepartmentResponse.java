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
public class DepartmentResponse {

    private Long id;
    private String name;
    private String code;
    private Long totalStudents;
    private Long totalTeachers;
    private Long totalCourses;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;
}