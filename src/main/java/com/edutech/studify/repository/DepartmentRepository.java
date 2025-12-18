package com.edutech.studify.repository;

import com.edutech.studify.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
    Optional<Department> findByCode(String code);
    boolean existsByName(String name);
    boolean existsByCode(String code);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.department.id = :departmentId AND s.deleted = false")
    long countStudentsByDepartmentId(Long departmentId);

    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.department.id = :departmentId AND t.deleted = false")
    long countTeachersByDepartmentId(Long departmentId);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.department.id = :departmentId")
    long countCoursesByDepartmentId(Long departmentId);
}