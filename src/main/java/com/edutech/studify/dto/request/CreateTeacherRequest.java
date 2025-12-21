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
public class CreateTeacherRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be valid (10-15 digits)")
    private String phone;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @Size(max = 100, message = "Specialization must not exceed 100 characters")
    private String specialization;
}