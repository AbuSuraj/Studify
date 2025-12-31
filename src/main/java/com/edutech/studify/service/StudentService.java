package com.edutech.studify.service;

import com.edutech.studify.dto.request.CreateStudentRequest;
import com.edutech.studify.dto.request.UpdateStudentRequest;
import com.edutech.studify.dto.response.PageResponse;
import com.edutech.studify.dto.response.StudentResponse;
import com.edutech.studify.dto.util.DtoMapper;
import com.edutech.studify.entity.*;
import com.edutech.studify.exception.DuplicateResourceException;
import com.edutech.studify.exception.ResourceNotFoundException;
import com.edutech.studify.repository.DepartmentRepository;
import com.edutech.studify.repository.StudentRepository;
import com.edutech.studify.repository.UserRepository;
import com.edutech.studify.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper dtoMapper;
    private final SecurityUtils securityUtils;

    /**
     * Create a new student
     * Only ADMIN can create students
     */
    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        // Check if email already exists
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Student", "email", request.getEmail());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Check if department exists
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        // Create user account
        String username = generateUsername(request.getFirstName(), request.getLastName());
        String defaultPassword = "Student@123"; // Should be changed on first login

        User user = User.builder()
                .username(username)
                .email(request.getEmail())
                .password(passwordEncoder.encode(defaultPassword))
                .role(Role.STUDENT)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // Create student
        Student student = Student.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .department(department)
                .enrollmentDate(request.getEnrollmentDate())
                .status(StudentStatus.ACTIVE)
                .deleted(false)
                .build();

        student = studentRepository.save(student);

        return dtoMapper.toStudentResponse(student);
    }

    /**
     * Get all students with pagination and filters
     */
    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> getAllStudents(
            String search,
            Long departmentId,
            StudentStatus status,
            Pageable pageable) {

        Page<Student> studentPage;

        if (search != null || departmentId != null || status != null) {
            // Apply filters
            studentPage = studentRepository.searchStudentsWithFilters(search, departmentId, status, pageable);
        } else {
            // Get all
            studentPage = studentRepository.findAll(pageable);
        }

        Page<StudentResponse> responsePage = studentPage.map(dtoMapper::toStudentResponse);

        return dtoMapper.toPageResponse(responsePage);
    }

    /**
     * Get student by ID
     * ADMIN and TEACHER can view any student
     * STUDENT can only view their own profile
     */
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Verify access
        securityUtils.verifyStudentAccess(student.getUser().getId());

        return dtoMapper.toStudentResponse(student);
    }

    /**
     * Get current student profile
     * For logged-in students
     */
    @Transactional(readOnly = true)
    public StudentResponse getMyProfile() {
        User currentUser = securityUtils.getCurrentUser();

        Student student = studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        return dtoMapper.toStudentResponse(student);
    }

    /**
     * Update student
     * ADMIN can update any field
     * STUDENT can only update phone and address
     */
    @Transactional
    public StudentResponse updateStudent(Long id, UpdateStudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        User currentUser = securityUtils.getCurrentUser();
        boolean isAdmin = securityUtils.isAdmin();

        // Students can only update their own profile
        if (!isAdmin && !student.getUser().getId().equals(currentUser.getId())) {
            securityUtils.verifyStudentAccess(student.getUser().getId());
        }

        // Update fields based on role
        if (isAdmin) {
            // Admin can update all fields
            if (request.getFirstName() != null) {
                student.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null) {
                student.setLastName(request.getLastName());
            }
            if (request.getEmail() != null && !request.getEmail().equals(student.getEmail())) {
                // Check for duplicate email
                if (studentRepository.existsByEmail(request.getEmail())) {
                    throw new DuplicateResourceException("Student", "email", request.getEmail());
                }
                student.setEmail(request.getEmail());
                student.getUser().setEmail(request.getEmail());
            }
            if (request.getDepartmentId() != null) {
                Department department = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
                student.setDepartment(department);
            }
        }

        // Both admin and student can update these
        if (request.getPhone() != null) {
            student.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            student.setAddress(request.getAddress());
        }

        student = studentRepository.save(student);

        return dtoMapper.toStudentResponse(student);
    }

    /**
     * Soft delete student
     * Only ADMIN can delete students
     */
    @Transactional
    public void deleteStudent(Long id) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Set soft delete fields
        student.setDeleted(true);
        student.setDeletedAt(LocalDateTime.now());
        student.setDeletedBy(securityUtils.getCurrentUsername());
        student.setStatus(StudentStatus.INACTIVE);

        // Deactivate user account
        student.getUser().setIsActive(false);

        studentRepository.save(student);
    }

    /**
     * Restore deleted student
     * Only ADMIN can restore students
     */
    @Transactional
    public StudentResponse restoreStudent(Long id) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        // Need to use custom query to find deleted students
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Restore student
        student.setDeleted(false);
        student.setDeletedAt(null);
        student.setDeletedBy(null);
        student.setStatus(StudentStatus.ACTIVE);

        // Reactivate user account
        student.getUser().setIsActive(true);

        student = studentRepository.save(student);

        return dtoMapper.toStudentResponse(student);
    }

    /**
     * Generate unique username from name
     */
    private String generateUsername(String firstName, String lastName) {
        String baseUsername = (firstName + "." + lastName).toLowerCase().replaceAll("\\s+", "");
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}
