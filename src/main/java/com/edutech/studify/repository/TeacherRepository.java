package com.edutech.studify.repository;

import com.edutech.studify.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByUserId(Long userId);
    boolean existsByEmail(String email);

    Page<Teacher> findByDepartmentId(Long departmentId, Pageable pageable);



    @Query("SELECT t FROM Teacher t WHERE " +
            "(:search IS NULL OR LOWER(CONCAT(t.firstName, ' ', t.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:departmentId IS NULL OR t.department.id = :departmentId)")
    Page<Teacher> searchTeachersWithFilters(
            @Param("search") String search,
            @Param("departmentId") Long departmentId,
            Pageable pageable
    );

    @Query("SELECT t FROM Teacher t WHERE t.deleted = true")
    Page<Teacher> findDeletedTeachers(Pageable pageable);
}
