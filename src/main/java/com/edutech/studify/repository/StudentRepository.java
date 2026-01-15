package com.edutech.studify.repository;

import com.edutech.studify.entity.Student;
import com.edutech.studify.entity.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
/**Handles all database operations for students. Spring Data JPA auto-generates implementations.*/
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    /**
   WHY: Check if email already exists before creating student
   AUTO-GENERATED SQL:
   SELECT * FROM students WHERE email = ? AND deleted = false

   BUSINESS NEED: Prevent duplicate emails
   */
    Optional<Student> findByUserId(Long userId);
    boolean existsByEmail(String email);
      /**
    WHY: Quick check without loading entire student object
    RETURNS: true/false (faster than findByEmail)
    BUSINESS NEED: Validation before creating/updating student
    */

    Page<Student> findByDepartmentId(Long departmentId, Pageable pageable);
    /**
   WHY: List all students in a department
   PAGEABLE: Returns 20 students at a time (not all 10,000!)

   BUSINESS SCENARIO:
   Admin: "Show me all Computer Science students"
   Query: findByDepartmentId(1, PageRequest.of(0, 20))
   Returns: Page 1 of students (1-20 of 500 total)
   */
    Page<Student> findByStatus(StudentStatus status, Pageable pageable);
    Page<Student> findByDepartmentIdAndStatus(Long departmentId, StudentStatus status, Pageable pageable);

    @Query("SELECT s FROM Student s WHERE " +
            "LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Student> searchStudents(@Param("search") String search, Pageable pageable);

    @Query("SELECT s FROM Student s WHERE " +
            "(:search IS NULL OR LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:departmentId IS NULL OR s.department.id = :departmentId) AND " +
            "(:status IS NULL OR s.status = :status)")
    Page<Student> searchStudentsWithFilters(
            @Param("search") String search,
            @Param("departmentId") Long departmentId,
            @Param("status") StudentStatus status,
            Pageable pageable
    );

    long countByStatus(StudentStatus status);
    long countByDepartmentId(Long departmentId);

    @Query("SELECT s FROM Student s WHERE s.deleted = true")
    Page<Student> findDeletedStudents(Pageable pageable);
}