package com.edutech.studify.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeRequest {

    @NotNull(message = "Enrollment ID is required")
    private Long enrollmentId;

    @NotBlank(message = "Grade is required")
    @Size(max = 5, message = "Grade must not exceed 5 characters")
    @Pattern(regexp = "^(A\\+|A|A-|B\\+|B|B-|C\\+|C|D|F)$",
            message = "Grade must be one of: A+, A, A-, B+, B, B-, C+, C, D, F")
    private String grade;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;

    private LocalDate gradedDate;
}