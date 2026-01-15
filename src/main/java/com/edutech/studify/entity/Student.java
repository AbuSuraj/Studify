package com.edutech.studify.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_student_email", columnList = "email"),
        @Index(name = "idx_student_status", columnList = "status"),
        @Index(name = "idx_student_department", columnList = "department_id")
})
@SQLDelete(sql = "UPDATE students SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // USER RELATIONSHIP
    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;
    /**
     WHY: Links to authentication system
     BUSINESS NEED: Students need login credentials
     ONE-TO-ONE: Each student has exactly one user account

     Database structure:
     students table: id=1, user_id=10, first_name="John"
     users table:    id=10, email="john@example.com", password="..."
     */

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be valid (10-15 digits)")
    @Column(nullable = false, length = 15)
    private String phone;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(columnDefinition = "TEXT")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
  /**
    WHY: Student belongs to a department
    MANY-TO-ONE: Many students → One department
    LAZY: Don't load department unless explicitly needed

    Example:
    Department: Computer Science (id=1)
    Students: John (dept_id=1), Jane (dept_id=1), Bob (dept_id=1)
    */


    @NotNull(message = "Enrollment date is required")
    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StudentStatus status = StudentStatus.ACTIVE;

    // Soft Delete Fields
    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;
    /**
    WHY: Soft delete instead of hard delete
    HARD DELETE: DELETE FROM students WHERE id=1 (data lost forever!)
    SOFT DELETE: UPDATE students SET deleted=true WHERE id=1 (data preserved)

    BUSINESS BENEFITS:
    - Can restore accidentally deleted students
    - Maintain data integrity (enrollments, grades still reference student)
    - Audit trail (who deleted, when)
    - Compliance (some regulations require data retention)

    @SQLRestriction("deleted = false") ensures:
    - findAll() only returns non-deleted students
    - findById(1) returns null if student is deleted
    */

    // Relationships
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments = new ArrayList<>();
    /**
        WHY: Student can enroll in multiple courses
        ONE-TO-MANY: One student → Many enrollments
        CASCADE: If student deleted, delete their enrollments too
        mappedBy: "student" field in Enrollment entity owns the relationship

        Example:
        Student John (id=1)
        - Enrollment 1: Math 101
        - Enrollment 2: Physics 101
        - Enrollment 3: Chemistry 101
        */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
     /**
    WHY: Convenience method for displaying name
    TRANSIENT: Not a database column, just a utility
    BUSINESS NEED: Show "John Doe" instead of concatenating everywhere
    */
}