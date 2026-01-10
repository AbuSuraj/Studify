package com.edutech.studify.service;

import com.edutech.studify.dto.request.GradeRequest;
import com.edutech.studify.dto.response.GradeResponse;
import com.edutech.studify.dto.response.PageResponse;
import com.edutech.studify.dto.util.DtoMapper;
import com.edutech.studify.entity.*;
import com.edutech.studify.exception.BusinessException;
import com.edutech.studify.exception.ResourceNotFoundException;
import com.edutech.studify.repository.EnrollmentRepository;
import com.edutech.studify.repository.GradeRepository;
import com.edutech.studify.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
public class GradeService {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final DtoMapper dtoMapper;
    private final SecurityUtils securityUtils;

    /**
     * Add or update grade for enrollment
     * TEACHER can grade students in their courses
     */
    @Transactional
    public GradeResponse addOrUpdateGrade(GradeRequest request) {
        log.info("Adding/updating grade for enrollment ID: {}", request.getEnrollmentId());

        // Get enrollment
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", request.getEnrollmentId()));

        // Check if enrollment is active
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new BusinessException("Cannot grade dropped enrollment");
        }

        // Verify teacher access
        if (securityUtils.isTeacher()) {
            User currentUser = securityUtils.getCurrentUser();
            if (enrollment.getCourse().getTeacher() == null ||
                    !enrollment.getCourse().getTeacher().getUser().getId().equals(currentUser.getId())) {
                throw new BusinessException("You can only grade students in your own courses");
            }
        }

        // Check if grade already exists
        Grade grade = gradeRepository.findByEnrollmentId(request.getEnrollmentId())
                .orElse(null);

        if (grade == null) {
            // Create new grade
            grade = Grade.builder()
                    .enrollment(enrollment)
                    .remarks(request.getRemarks())
                    .gradedDate(request.getGradedDate() != null ? request.getGradedDate() : LocalDate.now())
                    .build();
        } else {
            // Update existing grade
            grade.setRemarks(request.getRemarks());
            if (request.getGradedDate() != null) {
                grade.setGradedDate(request.getGradedDate());
            }
        }

        // Set grade and calculate grade point
        grade.setGradeWithPoint(request.getGrade());

        Grade savedGrade = gradeRepository.save(grade);
        log.info("Grade saved successfully for enrollment ID: {}", request.getEnrollmentId());

        return dtoMapper.toGradeResponse(savedGrade);
    }

    /**
     * Get grade by ID
     */
    @Transactional(readOnly = true)
    public GradeResponse getGradeById(Long id) {
        log.info("Fetching grade with ID: {}", id);

        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", id));

        // Verify access for students
        if (securityUtils.isStudent()) {
            if (!grade.getEnrollment().getStudent().getUser().getId().equals(securityUtils.getCurrentUserId())) {
                throw new BusinessException("You can only view your own grades");
            }
        }

        // Verify access for teachers
        if (securityUtils.isTeacher()) {
            User currentUser = securityUtils.getCurrentUser();
            if (grade.getEnrollment().getCourse().getTeacher() == null ||
                    !grade.getEnrollment().getCourse().getTeacher().getUser().getId().equals(currentUser.getId())) {
                throw new BusinessException("You can only view grades for your own courses");
            }
        }

        return dtoMapper.toGradeResponse(grade);
    }

    /**
     * Get grades by student
     * ADMIN, TEACHER (their courses), STUDENT (own)
     */
    @Transactional(readOnly = true)
    public PageResponse<GradeResponse> getGradesByStudent(
            Long studentId, String semester, int page, int size) {

        log.info("Fetching grades for student ID: {}, semester: {}", studentId, semester);

        // Verify access for students
        if (securityUtils.isStudent()) {
            List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
            if (!enrollments.isEmpty()) {
                if (!enrollments.get(0).getStudent().getUser().getId().equals(securityUtils.getCurrentUserId())) {
                    throw new BusinessException("You can only view your own grades");
                }
            }
        }

        Page<Grade> gradePage;

        if (semester != null) {
            // Get list and convert to page manually
            List<Grade> grades = gradeRepository.findByStudentIdAndSemester(studentId, semester);

            // Apply pagination manually
            Pageable pageable = PageRequest.of(page, size, Sort.by("gradedDate").descending());
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), grades.size());

            List<Grade> pageContent = grades.subList(start, end);
            gradePage = new PageImpl<>(pageContent, pageable, grades.size());
        } else {
            Pageable pageable = PageRequest.of(page, size, Sort.by("gradedDate").descending());
            gradePage = gradeRepository.findByStudentId(studentId, pageable);
        }

        Page<GradeResponse> responsePage = gradePage.map(dtoMapper::toGradeResponse);
        return dtoMapper.toPageResponse(responsePage);
    }

    /**
     * Get grades by course
     * ADMIN and TEACHER (own courses)
     */
    @Transactional(readOnly = true)
    public PageResponse<GradeResponse> getGradesByCourse(Long courseId, int page, int size) {
        log.info("Fetching grades for course ID: {}", courseId);

        // Verify access for teachers
        if (securityUtils.isTeacher()) {
            List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
            if (enrollments.isEmpty()) {
                throw new ResourceNotFoundException("Course", "id", courseId);
            }

            User currentUser = securityUtils.getCurrentUser();
            Course course = enrollments.get(0).getCourse();
            if (course.getTeacher() == null ||
                    !course.getTeacher().getUser().getId().equals(currentUser.getId())) {
                throw new BusinessException("You can only view grades for your own courses");
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("gradedDate").descending());
        Page<Grade> gradePage = gradeRepository.findByCourseId(courseId, pageable);

        Page<GradeResponse> responsePage = gradePage.map(dtoMapper::toGradeResponse);
        return dtoMapper.toPageResponse(responsePage);
    }

    /**
     * Get all grades for a student (without pagination)
     */
    @Transactional(readOnly = true)
    public List<GradeResponse> getAllGradesByStudent(Long studentId, String semester) {
        log.info("Fetching all grades for student ID: {}, semester: {}", studentId, semester);

        // Verify access for students
        if (securityUtils.isStudent()) {
            List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
            if (!enrollments.isEmpty()) {
                if (!enrollments.get(0).getStudent().getUser().getId().equals(securityUtils.getCurrentUserId())) {
                    throw new BusinessException("You can only view your own grades");
                }
            }
        }

        List<Grade> grades;
        if (semester != null) {
            grades = gradeRepository.findByStudentIdAndSemester(studentId, semester);
        } else {
            grades = gradeRepository.findByStudentId(studentId);
        }

        return grades.stream()
                .map(dtoMapper::toGradeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete grade
     * ADMIN only
     */
    @Transactional
    public void deleteGrade(Long id) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        log.info("Deleting grade with ID: {}", id);

        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", id));

        gradeRepository.delete(grade);
        log.info("Grade deleted successfully");
    }
}