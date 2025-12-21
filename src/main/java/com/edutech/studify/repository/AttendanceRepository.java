package com.edutech.studify.repository;

import com.edutech.studify.entity.Attendance;
import com.edutech.studify.entity.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Find attendance by enrollment and date
    Optional<Attendance> findByEnrollmentIdAndDate(Long enrollmentId, LocalDate date);

    // Check if attendance exists for enrollment and date
    boolean existsByEnrollmentIdAndDate(Long enrollmentId, LocalDate date);

    // Find attendance by enrollment
    List<Attendance> findByEnrollmentId(Long enrollmentId);

    // Find attendance by enrollment with pagination
    Page<Attendance> findByEnrollmentId(Long enrollmentId, Pageable pageable);

    // Find attendance by student
    @Query("SELECT a FROM Attendance a WHERE a.enrollment.student.id = :studentId")
    List<Attendance> findByStudentId(@Param("studentId") Long studentId);

    // Find attendance by student with pagination
    @Query("SELECT a FROM Attendance a WHERE a.enrollment.student.id = :studentId")
    Page<Attendance> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    // Find attendance by student and course
    @Query("SELECT a FROM Attendance a WHERE a.enrollment.student.id = :studentId AND a.enrollment.course.id = :courseId")
    List<Attendance> findByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    // Find attendance by course
    @Query("SELECT a FROM Attendance a WHERE a.enrollment.course.id = :courseId")
    List<Attendance> findByCourseId(@Param("courseId") Long courseId);

    // Find attendance by course and date
    @Query("SELECT a FROM Attendance a WHERE a.enrollment.course.id = :courseId AND a.date = :date")
    List<Attendance> findByCourseIdAndDate(@Param("courseId") Long courseId, @Param("date") LocalDate date);

    // Find attendance by student, course, and date range
    @Query("SELECT a FROM Attendance a WHERE " +
            "a.enrollment.student.id = :studentId AND " +
            "a.enrollment.course.id = :courseId AND " +
            "a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByStudentIdAndCourseIdAndDateRange(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Find attendance by date range
    @Query("SELECT a FROM Attendance a WHERE a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Count attendance by student, course, and status
    @Query("SELECT COUNT(a) FROM Attendance a WHERE " +
            "a.enrollment.student.id = :studentId AND " +
            "a.enrollment.course.id = :courseId AND " +
            "a.status = :status")
    long countByStudentIdAndCourseIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("status") AttendanceStatus status
    );

    // Calculate attendance percentage for enrollment
    @Query("SELECT " +
            "CAST(COUNT(CASE WHEN a.status = 'PRESENT' OR a.status = 'LATE' THEN 1 END) AS double) * 100.0 / COUNT(a) " +
            "FROM Attendance a WHERE a.enrollment.id = :enrollmentId")
    Double calculateAttendancePercentageByEnrollmentId(@Param("enrollmentId") Long enrollmentId);

    // Calculate attendance percentage for student in course
    @Query("SELECT " +
            "CAST(COUNT(CASE WHEN a.status = 'PRESENT' OR a.status = 'LATE' THEN 1 END) AS double) * 100.0 / COUNT(a) " +
            "FROM Attendance a WHERE a.enrollment.student.id = :studentId AND a.enrollment.course.id = :courseId")
    Double calculateAttendancePercentageByStudentIdAndCourseId(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId
    );

    // Get attendance statistics for course
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.enrollment.course.id = :courseId AND a.date = :date GROUP BY a.status")
    List<Object[]> getAttendanceStatisticsByCourseIdAndDate(@Param("courseId") Long courseId, @Param("date") LocalDate date);
}
