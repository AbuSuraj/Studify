package com.edutech.studify.dto.response;

import com.edutech.studify.entity.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private DepartmentSummary department;
    private LocalDate enrollmentDate;
    private StudentStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;
 /**
    WHY THIS STRUCTURE:

    1. Computed fields:
       - fullName: firstName + " " + lastName
       - Client doesn't need to concatenate
    
    2. Nested DTOs:
       - department: Only id, name, code (not entire Department entity)
       - Reduces payload size
       - Prevents circular references

    3. Audit fields:
       - createdAt, createdBy: Who created, when
       - lastModifiedAt, lastModifiedBy: Last update
       - Transparency for admin

    4. Missing fields (intentional):
       - No password (security!)
       - No deleted/deletedAt fields (shouldn't see on active student)
    */

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
