package com.edutech.studify.service;

import com.edutech.studify.dto.request.CreateTeacherRequest;
import com.edutech.studify.dto.request.UpdateTeacherRequest;
import com.edutech.studify.dto.response.PageResponse;
import com.edutech.studify.dto.response.TeacherResponse;
import com.edutech.studify.dto.util.DtoMapper;
import com.edutech.studify.entity.Department;
import com.edutech.studify.entity.Role;
import com.edutech.studify.entity.Teacher;
import com.edutech.studify.entity.User;
import com.edutech.studify.exception.BusinessException;
import com.edutech.studify.exception.DuplicateResourceException;
import com.edutech.studify.exception.ResourceNotFoundException;
import com.edutech.studify.repository.DepartmentRepository;
import com.edutech.studify.repository.TeacherRepository;
import com.edutech.studify.repository.UserRepository;
import com.edutech.studify.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DtoMapper dtoMapper;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new teacher with user account
     * Only ADMIN can create teachers
     */
    @Transactional
    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        log.info("Creating new teacher with email: {}", request.getEmail());

        // Check if email already exists
        if (teacherRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Teacher", "email", request.getEmail());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Get department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        // Create user account
        String username = generateUsername(request.getFirstName(), request.getLastName());
        String defaultPassword = "Teacher@123"; // Should be changed on first login

        User user = User.builder()
                .username(username)
                .email(request.getEmail())
                .password(passwordEncoder.encode(defaultPassword))
                .role(Role.TEACHER)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // Create teacher profile
        Teacher teacher = Teacher.builder()
                .user(savedUser)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .department(department)
                .specialization(request.getSpecialization())
                .deleted(false)
                .build();

        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("Teacher created successfully with ID: {} and username: {}", savedTeacher.getId(), username);

        return dtoMapper.toTeacherResponse(savedTeacher);
    }

    /**
     * Get all teachers with pagination
     * Accessible by ADMIN
     */
    @Transactional(readOnly = true)
    public PageResponse<TeacherResponse> getAllTeachers(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching all teachers - Page: {}, Size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Teacher> teacherPage = teacherRepository.findAll(pageable);

        Page<TeacherResponse> responsePage = teacherPage.map(dtoMapper::toTeacherResponse);
        return dtoMapper.toPageResponse(responsePage);
    }

    /**
     * Get teacher by ID
     * Accessible by ADMIN, TEACHER (own profile), STUDENT (can view their course teachers)
     */
    @Transactional(readOnly = true)
    public TeacherResponse getTeacherById(Long id) {
        log.info("Fetching teacher with ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        return dtoMapper.toTeacherResponse(teacher);
    }

    /**
     * Get teacher by user ID
     * Accessible by the teacher themselves and ADMIN
     */
    @Transactional(readOnly = true)
    public TeacherResponse getTeacherByUserId(Long userId) {
        log.info("Fetching teacher with user ID: {}", userId);

        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "userId", userId));

        // Verify access - teacher can only view their own profile
        if (!securityUtils.isAdmin() && !securityUtils.getCurrentUserId().equals(userId)) {
            throw new BusinessException("You can only access your own profile");
        }

        return dtoMapper.toTeacherResponse(teacher);
    }

    /**
     * Search teachers with filters
     * Accessible by ADMIN
     */
    @Transactional(readOnly = true)
    public PageResponse<TeacherResponse> searchTeachers(
            String search, Long departmentId, int page, int size, String sortBy, String sortDir) {

        log.info("Searching teachers with keyword: {}, departmentId: {}", search, departmentId);

        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Teacher> teacherPage = teacherRepository.searchTeachersWithFilters(
                search, departmentId, pageable);

        Page<TeacherResponse> responsePage = teacherPage.map(dtoMapper::toTeacherResponse);
        return dtoMapper.toPageResponse(responsePage);
    }

    /**
     * Get teachers by department
     * Accessible by ADMIN
     */
    @Transactional(readOnly = true)
    public PageResponse<TeacherResponse> getTeachersByDepartment(
            Long departmentId, int page, int size, String sortBy, String sortDir) {

        log.info("Fetching teachers for department ID: {}", departmentId);

        // Verify department exists
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Teacher> teacherPage = teacherRepository.findByDepartmentId(departmentId, pageable);

        Page<TeacherResponse> responsePage = teacherPage.map(dtoMapper::toTeacherResponse);
        return dtoMapper.toPageResponse(responsePage);
    }

    /**
     * Update teacher
     * Only ADMIN can update full profile, TEACHER can update limited fields
     */
    @Transactional
    public TeacherResponse updateTeacher(Long id, UpdateTeacherRequest request) {
        log.info("Updating teacher with ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        // Check if current user is the teacher themselves
        boolean isOwnProfile = securityUtils.isTeacher() &&
                teacher.getUser().getId().equals(securityUtils.getCurrentUserId());

        // Only ADMIN can do full update, teacher can only update limited fields
        if (!securityUtils.isAdmin() && !isOwnProfile) {
            throw new BusinessException("You don't have permission to update this teacher");
        }

        // Update fields if provided
        if (request.getFirstName() != null) {
            if (isOwnProfile && !securityUtils.isAdmin()) {
                throw new BusinessException("You cannot update your name. Contact admin.");
            }
            teacher.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            if (isOwnProfile && !securityUtils.isAdmin()) {
                throw new BusinessException("You cannot update your name. Contact admin.");
            }
            teacher.setLastName(request.getLastName());
        }

        if (request.getEmail() != null && !request.getEmail().equals(teacher.getEmail())) {
            if (isOwnProfile && !securityUtils.isAdmin()) {
                throw new BusinessException("You cannot update your email. Contact admin.");
            }
            if (teacherRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Teacher", "email", request.getEmail());
            }
            teacher.setEmail(request.getEmail());
            teacher.getUser().setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            teacher.setPhone(request.getPhone());
        }

        if (request.getDepartmentId() != null) {
            if (isOwnProfile && !securityUtils.isAdmin()) {
                throw new BusinessException("You cannot update your department. Contact admin.");
            }
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            teacher.setDepartment(department);
        }

        if (request.getSpecialization() != null) {
            teacher.setSpecialization(request.getSpecialization());
        }

        Teacher updatedTeacher = teacherRepository.save(teacher);
        log.info("Teacher updated successfully with ID: {}", updatedTeacher.getId());

        return dtoMapper.toTeacherResponse(updatedTeacher);
    }

    /**
     * Soft delete teacher
     * Only ADMIN can delete teachers
     */
    @Transactional
    public void deleteTeacher(Long id) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        log.info("Deleting teacher with ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        // Check if teacher has active courses
        if (teacher.getCourses() != null && !teacher.getCourses().isEmpty()) {
            throw new BusinessException(
                    "Cannot delete teacher with active courses. Please reassign courses first. " +
                            "Current course count: " + teacher.getCourses().size()
            );
        }

        // Soft delete
        teacher.setDeleted(true);
        teacher.setDeletedAt(LocalDateTime.now());
        teacher.setDeletedBy(securityUtils.getCurrentUsername());
        teacher.getUser().setIsActive(false);

        teacherRepository.save(teacher);
        log.info("Teacher soft deleted successfully with ID: {}", id);
    }

    /**
     * Restore soft-deleted teacher
     * Only ADMIN can restore teachers
     */
    @Transactional
    public TeacherResponse restoreTeacher(Long id) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        log.info("Restoring teacher with ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        if (!teacher.getDeleted()) {
            throw new BusinessException("Teacher is not deleted");
        }

        teacher.setDeleted(false);
        teacher.setDeletedAt(null);
        teacher.setDeletedBy(null);
        teacher.getUser().setIsActive(true);

        Teacher restoredTeacher = teacherRepository.save(teacher);
        log.info("Teacher restored successfully with ID: {}", id);

        return dtoMapper.toTeacherResponse(restoredTeacher);
    }

    /**
     * Get deleted teachers
     * Only ADMIN can view deleted teachers
     */
    @Transactional(readOnly = true)
    public PageResponse<TeacherResponse> getDeletedTeachers(int page, int size) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        log.info("Fetching deleted teachers");

        Pageable pageable = PageRequest.of(page, size, Sort.by("deletedAt").descending());
        Page<Teacher> teacherPage = teacherRepository.findDeletedTeachers(pageable);

        Page<TeacherResponse> responsePage = teacherPage.map(dtoMapper::toTeacherResponse);
        return dtoMapper.toPageResponse(responsePage);
    }

    // ================ Helper Methods ================

    /**
     * Generate unique username from name
     */
    private String generateUsername(String firstName, String lastName) {
        String baseUsername = (firstName + "." + lastName).toLowerCase()
                .replaceAll("[^a-z.]", "");

        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}
