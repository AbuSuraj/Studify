package com.edutech.studify.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStudentRequest {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be valid (10-15 digits)")
    private String phone;

    private String address;

    private Long departmentId;
}
/**
 * WHY SEPARATE DTO FOR UPDATE?
 * <p>
 * 1. Different validation rules:
 * - Create: All fields required
 * - Update: All fields optional (partial update)
 * <p>
 * 2. Different fields allowed:
 * - Create: Sets initial values (enrollment date, etc.)
 * - Update: Can't change enrollment date
 * <p>
 * 3. Different permissions:
 * - ADMIN: Can update all fields
 * - STUDENT: Can only update phone and address
 */