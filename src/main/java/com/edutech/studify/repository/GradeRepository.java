package com.edutech.studify.repository;

import com.edutech.studify.entity.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    Optional<Grade> findByEnrollmentId(Long enrollmentId);
    boolean existsByEnrollmentId(Long enrollmentId);

    @Query("SELECT g FROM Grade g WHERE g.enrollment.student.id = :studentId")
    List<Grade> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT g FROM Grade g WHERE g.enrollment.student.id = :studentId")
    Page<Grade> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    @Query("SELECT g FROM Grade g WHERE g.enrollment.course.id = :courseId")
    List<Grade> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT g FROM Grade g WHERE g.enrollment.course.id = :courseId")
    Page<Grade> findByCourseId(@Param("courseId") Long courseId, Pageable pageable);

    @Query("SELECT g FROM Grade g WHERE g.enrollment.student.id = :studentId AND g.enrollment.course.semester = :semester")
    List<Grade> findByStudentIdAndSemester(@Param("studentId") Long studentId, @Param("semester") String semester);

    @Query("SELECT AVG(g.gradePoint) FROM Grade g WHERE g.enrollment.student.id = :studentId")
    BigDecimal calculateGPAByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT AVG(g.gradePoint) FROM Grade g WHERE g.enrollment.student.id = :studentId AND g.enrollment.course.semester = :semester")
    BigDecimal calculateGPAByStudentIdAndSemester(@Param("studentId") Long studentId, @Param("semester") String semester);

    @Query("SELECT AVG(g.gradePoint) FROM Grade g WHERE g.enrollment.course.id = :courseId")
    BigDecimal calculateAverageGradeByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT g.enrollment.student.id, AVG(g.gradePoint) as avgGPA FROM Grade g " +
            "GROUP BY g.enrollment.student.id " +
            "ORDER BY avgGPA DESC")
    Page<Object[]> findTopPerformingStudents(Pageable pageable);

    @Query("SELECT g.grade, COUNT(g) FROM Grade g WHERE g.enrollment.course.id = :courseId GROUP BY g.grade")
    List<Object[]> countGradeDistributionByCourseId(@Param("courseId") Long courseId);
}