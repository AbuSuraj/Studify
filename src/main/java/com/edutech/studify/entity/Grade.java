package com.edutech.studify.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "grades", indexes = {
        @Index(name = "idx_grade_enrollment", columnList = "enrollment_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "enrollment_id", unique = true, nullable = false)
    private Enrollment enrollment;

    @NotBlank(message = "Grade is required")
    @Size(max = 5, message = "Grade must not exceed 5 characters")
    @Column(nullable = false, length = 5)
    private String grade;

    @Column(name = "grade_point", precision = 3, scale = 2)
    private BigDecimal gradePoint;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String remarks;

    @NotNull(message = "Graded date is required")
    @Column(name = "graded_date", nullable = false)
    private LocalDate gradedDate;

    // Helper method to set grade and automatically calculate grade point
    public void setGradeWithPoint(String gradeValue) {
        this.grade = gradeValue;
        this.gradePoint = calculateGradePoint(gradeValue);
    }

    // Grade point calculation based on letter grade
    private BigDecimal calculateGradePoint(String grade) {
        return switch (grade.toUpperCase()) {
            case "A+" -> new BigDecimal("4.0");
            case "A" -> new BigDecimal("3.7");
            case "A-" -> new BigDecimal("3.5");
            case "B+" -> new BigDecimal("3.25");
            case "B" -> new BigDecimal("3.0");
            case "B-" -> new BigDecimal("2.75");
            case "C+" -> new BigDecimal("2.5");
            case "C" -> new BigDecimal("2.25");
            case "D" -> new BigDecimal("2.00");
            case "F" -> new BigDecimal("0.0");
            default -> new BigDecimal("0.0");
        };
    }
}
