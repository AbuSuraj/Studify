package com.edutech.studify.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers", indexes = {
        @Index(name = "idx_teacher_email", columnList = "email"),
        @Index(name = "idx_teacher_department", columnList = "department_id")
})
@SQLDelete(sql = "UPDATE teachers SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
    WHY: Unique identifier for each teacher
    BUSINESS NEED: Reference in courses, track who teaches what
    */

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

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

    /**MANY-TO-ONE: Many teachers → One department*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
     /*
    WHY: Teacher belongs to a department
    MANY-TO-ONE: Many teachers → One department

    BUSINESS SCENARIO:
    - Computer Science department has 10 teachers
    - All 10 teachers have department_id = 1
    */

    /** department_id is a foreign key
    The Department entity is NOT loaded immediately
    📌 Only loaded when you do:
    student.getDepartment()

     one cons:
     ❌ Can cause LazyInitializationException if session is closed
     */

    @Size(max = 100, message = "Specialization must not exceed 100 characters")
    @Column(length = 100)
    private String specialization;

    // Soft Delete Fields
    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    // Relationships.
    /** ONE-TO-MANY: One teacher → Many courses */
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<Course> courses = new ArrayList<>();

    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
}