package com.edutech.studify.service;

import com.edutech.studify.dto.request.EnrollmentRequest;
import com.edutech.studify.dto.response.EnrollmentResponse;
import com.edutech.studify.dto.response.PageResponse;
import com.edutech.studify.dto.util.DtoMapper;
import com.edutech.studify.entity.*;
import com.edutech.studify.exception.BusinessException;
import com.edutech.studify.exception.DuplicateResourceException;
import com.edutech.studify.exception.ResourceNotFoundException;
import com.edutech.studify.repository.CourseRepository;
import com.edutech.studify.repository.EnrollmentRepository;
import com.edutech.studify.repository.StudentRepository;
import com.edutech.studify.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final DtoMapper dtoMapper;
    private final SecurityUtils securityUtils;

    /**
     * Enroll student in course
     * ADMIN can enroll any student, STUDENT can self-enroll
     */
    @Transactional
    public EnrollmentResponse enrollStudent(EnrollmentRequest request) {
        log.info("Enrolling student {} in course {}", request.getStudentId(), request.getCourseId());

        // Get student
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));

        // Check if student is active
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new BusinessException("Cannot enroll inactive student");
        }

        // Verify access - student can only enroll themselves
        if (securityUtils.isStudent()) {
            if (!student.getUser().getId().equals(securityUtils.getCurrentUserId())) {
                throw new BusinessException("You can only enroll yourself");
            }
        }

        // Get course
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));

        // Check if already enrolled
        if (enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                request.getStudentId(), request.getCourseId(), EnrollmentStatus.ACTIVE)) {
            throw new DuplicateResourceException("Student is already enrolled in this course");
        }

        // Check course capacity
        if (course.isFull()) {
            throw new BusinessException("Course is full. No available seats.");
        }

        // Create enrollment
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrollmentDate(LocalDate.now())
                .status(EnrollmentStatus.ACTIVE)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Student enrolled successfully. Enrollment ID: {}", savedEnrollment.getId());

        return dtoMapper.toEnrollmentResponse(savedEnrollment);
    }

    /**
     * Drop course (unenroll)
     * ADMIN can drop any enrollment, STUDENT can drop their own
     */
    @Transactional
    public void dropCourse(Long enrollmentId) {
        log.info("Dropping enrollment ID: {}", enrollmentId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        // Verify access
        if (securityUtils.isStudent()) {
            if (!enrollment.getStudent().getUser().getId().equals(securityUtils.getCurrentUserId())) {
                throw new BusinessException("You can only drop your own enrollments");
            }
        }

        // Check if already dropped
        if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
            throw new BusinessException("Enrollment is already dropped");
        }

        // Check if final grades are submitted
        if (enrollment.getGrade() != null) {
            throw new BusinessException("Cannot drop course after grades are submitted");
        }

        // Change status to DROPPED
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);

        log.info("Enrollment dropped successfully");
    }

    /**
     * Get enrollment by ID
     */
    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentById(Long id) {
        log.info("Fetching enrollment with ID: {}", id);

        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));

        // Verify access for students
        if (securityUtils.isStudent()) {
            if (!enrollment.getStudent().getUser().getId().equals(securityUtils.getCurrentUserId())) {
                throw new BusinessException("You can only view your own enrollments");
            }
        }

        return dtoMapper.toEnrollmentResponse(enrollment);
    }

    /**
     * Get student enrollments
     * ADMIN and TEACHER can view all, STUDENT can view own only
     */
    @Transactional(readOnly = true)
    public PageResponse<EnrollmentResponse> getStudentEnrollments(
            Long studentId, EnrollmentStatus status, int page, int size) {

        log.info("Fetching enrollments for student ID: {}, status: {}", studentId, status);

        // Verify student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        // Verify access for students
        if (securityUtils.isStudent()) {
            if (!student.getUser().getId().equals(securityUtils.getCurrentUserId())) {
                throw new BusinessException("You can only view your own enrollments");
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("enrollmentDate").descending());

        Page<Enrollment> enrollmentPage;
        if (status != null) {
            enrollmentPage = enrollmentRepository.findByStudentIdAndStatus(studentId, status, pageable);
        } else {
            enrollmentPage = enrollmentRepository.findByStudentId(studentId, pageable);
        }

        Page<EnrollmentResponse> responsePage = enrollmentPage.map(dtoMapper::toEnrollmentResponse);
        return dtoMapper.toPageResponse(responsePage);
    }

    /**
     * Get course enrollments
     * ADMIN and TEACHER (own courses) can view
     */
    @Transactional(readOnly = true)
    public PageResponse<EnrollmentResponse> getCourseEnrollments(
            Long courseId, EnrollmentStatus status, int page, int size) {

        log.info("Fetching enrollments for course ID: {}, status: {}", courseId, status);

        // Verify course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Verify access for teachers
        if (securityUtils.isTeacher()) {
            User currentUser = securityUtils.getCurrentUser();
            if (course.getTeacher() == null ||
                    !course.getTeacher().getUser().getId().equals(currentUser.getId())) {
                throw new BusinessException("You can only view enrollments for your own courses");
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("enrollmentDate").descending());

        Page<Enrollment> enrollmentPage;
        if (status != null) {
            enrollmentPage = enrollmentRepository.findByCourseIdAndStatus(courseId, status, pageable);
        } else {
            enrollmentPage = enrollmentRepository.findByCourseId(courseId, pageable);
        }

        Page<EnrollmentResponse> responsePage = enrollmentPage.map(dtoMapper::toEnrollmentResponse);
        return dtoMapper.toPageResponse(responsePage);
    }

    /**
     * Get active enrollments for student
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getActiveEnrollmentsByStudentId(Long studentId) {
        log.info("Fetching active enrollments for student ID: {}", studentId);

        // Verify student exists
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        // Verify access for students
        if (securityUtils.isStudent()) {
            Student student = studentRepository.findById(studentId).get();
            if (!student.getUser().getId().equals(securityUtils.getCurrentUserId())) {
                throw new BusinessException("You can only view your own enrollments");
            }
        }

        List<Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsByStudentId(studentId);
        return enrollments.stream()
                .map(dtoMapper::toEnrollmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active enrollments for course
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getActiveEnrollmentsByCourseId(Long courseId) {
        log.info("Fetching active enrollments for course ID: {}", courseId);

        // Verify course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Verify access for teachers
        if (securityUtils.isTeacher()) {
            User currentUser = securityUtils.getCurrentUser();
            if (course.getTeacher() == null ||
                    !course.getTeacher().getUser().getId().equals(currentUser.getId())) {
                throw new BusinessException("You can only view enrollments for your own courses");
            }
        }

        List<Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsByCourseId(courseId);
        return enrollments.stream()
                .map(dtoMapper::toEnrollmentResponse)
                .collect(Collectors.toList());
    }
}