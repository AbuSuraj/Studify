package com.edutech.studify.service;

import com.edutech.studify.dto.request.DepartmentRequest;
import com.edutech.studify.dto.response.DepartmentResponse;
import com.edutech.studify.dto.util.DtoMapper;
import com.edutech.studify.entity.Department;
import com.edutech.studify.exception.BusinessException;
import com.edutech.studify.exception.DuplicateResourceException;
import com.edutech.studify.exception.ResourceNotFoundException;
import com.edutech.studify.repository.DepartmentRepository;
import com.edutech.studify.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DtoMapper dtoMapper;
    private final SecurityUtils securityUtils;

    /**
     * Create a new department
     * Only ADMIN can create departments
     */
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        // Check if department name already exists
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }

        // Check if department code already exists
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Department", "code", request.getCode());
        }

        // Create department
        Department department = Department.builder()
                .name(request.getName())
                .code(request.getCode())
                .build();

        department = departmentRepository.save(department);

        // Get counts for response
        long studentCount = departmentRepository.countStudentsByDepartmentId(department.getId());
        long teacherCount = departmentRepository.countTeachersByDepartmentId(department.getId());
        long courseCount = departmentRepository.countCoursesByDepartmentId(department.getId());

        return dtoMapper.toDepartmentResponse(department, studentCount, teacherCount, courseCount);
    }

    /**
     * Get all departments
     * Accessible by all authenticated users
     */
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();

        return departments.stream()
                .map(dept -> {
                    long studentCount = departmentRepository.countStudentsByDepartmentId(dept.getId());
                    long teacherCount = departmentRepository.countTeachersByDepartmentId(dept.getId());
                    long courseCount = departmentRepository.countCoursesByDepartmentId(dept.getId());
                    return dtoMapper.toDepartmentResponse(dept, studentCount, teacherCount, courseCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get department by ID
     * Accessible by all authenticated users
     */
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        long studentCount = departmentRepository.countStudentsByDepartmentId(id);
        long teacherCount = departmentRepository.countTeachersByDepartmentId(id);
        long courseCount = departmentRepository.countCoursesByDepartmentId(id);

        return dtoMapper.toDepartmentResponse(department, studentCount, teacherCount, courseCount);
    }

    /**
     * Update department
     * Only ADMIN can update departments
     */
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Check if new name already exists (if changed)
        if (!department.getName().equals(request.getName()) &&
                departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }

        // Check if new code already exists (if changed)
        if (!department.getCode().equals(request.getCode()) &&
                departmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Department", "code", request.getCode());
        }

        // Update department
        department.setName(request.getName());
        department.setCode(request.getCode());

        department = departmentRepository.save(department);

        // Get counts
        long studentCount = departmentRepository.countStudentsByDepartmentId(id);
        long teacherCount = departmentRepository.countTeachersByDepartmentId(id);
        long courseCount = departmentRepository.countCoursesByDepartmentId(id);

        return dtoMapper.toDepartmentResponse(department, studentCount, teacherCount, courseCount);
    }

    /**
     * Delete department
     * Only ADMIN can delete departments
     * Cannot delete if department has students or courses
     */
    @Transactional
    public void deleteDepartment(Long id) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Check if department has active students
        long studentCount = departmentRepository.countStudentsByDepartmentId(id);
        if (studentCount > 0) {
            throw new BusinessException(
                    "Cannot delete department with active students. Please transfer students first. " +
                            "Current student count: " + studentCount
            );
        }

        // Check if department has courses
        long courseCount = departmentRepository.countCoursesByDepartmentId(id);
        if (courseCount > 0) {
            throw new BusinessException(
                    "Cannot delete department with courses. Please remove or transfer courses first. " +
                            "Current course count: " + courseCount
            );
        }

        departmentRepository.delete(department);
    }
}