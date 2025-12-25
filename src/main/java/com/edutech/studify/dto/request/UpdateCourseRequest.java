package com.edutech.studify.dto.request;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCourseRequest {

    @Size(min = 5, max = 100)
    private String name;

    private String description;

    @Min(1)
    @Max(6)
    private Integer credits;

    private String semester;

    @Min(10)
    @Max(200)
    private Integer maxCapacity;

    private Long departmentId;
    private Long teacherId;
}
