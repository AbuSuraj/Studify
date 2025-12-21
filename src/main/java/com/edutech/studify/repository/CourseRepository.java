package com.edutech.studify.repository;

import com.edutech.studify.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    boolean existsByCourseCode(String courseCode);

    Page<Course> findByDepartmentId(Long departmentId, Pageable pageable);
    Page<Course> findByTeacherId(Long teacherId, Pageable pageable);
    Page<Course> findBySemester(String semester, Pageable pageable);

    List<Course> findByTeacherId(Long teacherId);

    @Query("SELECT c FROM Course c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Course> searchCourses(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE " +
            "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:departmentId IS NULL OR c.department.id = :departmentId) AND " +
            "(:semester IS NULL OR c.semester = :semester) AND " +
            "(:teacherId IS NULL OR c.teacher.id = :teacherId)")
    Page<Course> searchCoursesWithFilters(
            @Param("search") String search,
            @Param("departmentId") Long departmentId,
            @Param("semester") String semester,
            @Param("teacherId") Long teacherId,
            Pageable pageable
    );

    long countByDepartmentId(Long departmentId);
    long countByTeacherId(Long teacherId);
    long countBySemester(String semester);

    @Query("SELECT c FROM Course c WHERE " +
            "(SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = c.id AND e.status = 'ACTIVE') < c.maxCapacity")
    Page<Course> findAvailableCourses(Pageable pageable);
}
