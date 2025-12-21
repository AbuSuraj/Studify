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

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    Optional<Student> findByUserId(Long userId);
    boolean existsByEmail(String email);

    Page<Student> findByDepartmentId(Long departmentId, Pageable pageable);
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