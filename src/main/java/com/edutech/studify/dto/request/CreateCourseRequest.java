package com.edutech.studify.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCourseRequest {

    @NotBlank(message = "Course code is required")
    @Size(min = 5, max = 10, message = "Course code must be between 5 and 10 characters")
    private String courseCode;

    @NotBlank(message = "Course name is required")
    @Size(min = 5, max = 100, message = "Course name must be between 5 and 100 characters")
    private String name;

    private String description;

    @NotNull(message = "Credits are required")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 6, message = "Credits must not exceed 6")
    private Integer credits;

    @NotBlank(message = "Semester is required")
    private String semester;

    @NotNull(message = "Maximum capacity is required")
    @Min(value = 10, message = "Maximum capacity must be at least 10")
    @Max(value = 200, message = "Maximum capacity must not exceed 200")
    private Integer maxCapacity;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private Long teacherId; // Optional
}