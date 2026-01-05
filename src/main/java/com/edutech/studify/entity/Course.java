package com.edutech.studify.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses", indexes = {
        @Index(name = "idx_course_code", columnList = "course_code"),
        @Index(name = "idx_course_department", columnList = "department_id"),
        @Index(name = "idx_course_teacher", columnList = "teacher_id"),
        @Index(name = "idx_course_semester", columnList = "semester")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Course code is required")
    @Size(min = 5, max = 10, message = "Course code must be between 5 and 10 characters")
    @Column(name = "course_code", unique = true, nullable = false, length = 10)
    private String courseCode;

    @NotBlank(message = "Course name is required")
    @Size(min = 5, max = 100, message = "Course name must be between 5 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Credits are required")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 6, message = "Credits must not exceed 6")
    @Column(nullable = false)
    private Integer credits;

    @NotBlank(message = "Semester is required")
    @Column(nullable = false, length = 20)
    private String semester;

    @NotNull(message = "Maximum capacity is required")
    @Min(value = 10, message = "Maximum capacity must be at least 10")
    @Max(value = 200, message = "Maximum capacity must not exceed 200")
    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    // Relationships
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default  // This tells Lombok to use the default value in builder
    private List<Enrollment> enrollments = new ArrayList<>();

    // Helper method to get enrolled student count
    @Transient
    public int getEnrolledCount() {
        if (enrollments == null) {
            return 0;
        }
        return (int) enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .count();
    }

    // Helper method to check if course is full
    @Transient
    public boolean isFull() {
        return getEnrolledCount() >= maxCapacity;
    }

    // Helper method to get available seats
    @Transient
    public int getAvailableSeats() {
        return maxCapacity - getEnrolledCount();
    }
}