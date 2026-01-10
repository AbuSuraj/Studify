package com.edutech.studify.service;

import com.edutech.studify.dto.request.AttendanceRequest;
import com.edutech.studify.dto.request.MarkAttendanceRequest;
import com.edutech.studify.dto.response.AttendanceResponse;
import com.edutech.studify.dto.response.AttendanceSummaryResponse;
import com.edutech.studify.dto.util.DtoMapper;
import com.edutech.studify.entity.*;
import com.edutech.studify.exception.BusinessException;
import com.edutech.studify.exception.DuplicateResourceException;
import com.edutech.studify.exception.ResourceNotFoundException;
import com.edutech.studify.repository.AttendanceRepository;
import com.edutech.studify.repository.CourseRepository;
import com.edutech.studify.repository.EnrollmentRepository;
import com.edutech.studify.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final DtoMapper dtoMapper;
    private final SecurityUtils securityUtils;

    /**
     * Mark attendance for multiple students
     * TEACHER can mark for their courses
     */
    @Transactional
    public AttendanceSummaryResponse markAttendance(MarkAttendanceRequest request) {
        log.info("Marking attendance for course ID: {} on date: {}",
                request.getCourseId(), request.getDate());

        // Verify date is not in future
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Cannot mark attendance for future date");
        }

        // Get course
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));

        // Verify teacher access
        if (securityUtils.isTeacher()) {
            User currentUser = securityUtils.getCurrentUser();
            if (course.getTeacher() == null ||
                    !course.getTeacher().getUser().getId().equals(currentUser.getId())) {
                throw new BusinessException("You can only mark attendance for your own courses");
            }
        }

        // Get all active enrollments for the course
        List<Enrollment> activeEnrollments = enrollmentRepository.findActiveEnrollmentsByCourseId(request.getCourseId());

        // Verify all enrollment IDs belong to this course
        List<Long> activeEnrollmentIds = activeEnrollments.stream()
                .map(Enrollment::getId)
                .collect(Collectors.toList());

        for (AttendanceRequest record : request.getAttendanceRecords()) {
            if (!activeEnrollmentIds.contains(record.getEnrollmentId())) {
                throw new BusinessException("Enrollment ID " + record.getEnrollmentId() +
                        " does not belong to this course or is not active");
            }
        }

        // Save attendance records
        List<Attendance> attendanceList = new ArrayList<>();
        int present = 0, absent = 0, late = 0;

        for (AttendanceRequest record : request.getAttendanceRecords()) {
            // Check if attendance already exists for this enrollment and date
            if (attendanceRepository.existsByEnrollmentIdAndDate(record.getEnrollmentId(), request.getDate())) {
                // Update existing attendance
                Attendance existing = attendanceRepository.findByEnrollmentIdAndDate(
                        record.getEnrollmentId(), request.getDate()).get();
                existing.setStatus(record.getStatus());
                attendanceList.add(attendanceRepository.save(existing));
            } else {
                // Create new attendance
                Enrollment enrollment = enrollmentRepository.findById(record.getEnrollmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", record.getEnrollmentId()));

                Attendance attendance = Attendance.builder()
                        .enrollment(enrollment)
                        .date(request.getDate())
                        .status(record.getStatus())
                        .build();

                attendanceList.add(attendanceRepository.save(attendance));
            }

            // Count statuses
            switch (record.getStatus()) {
                case PRESENT -> present++;
                case ABSENT -> absent++;
                case LATE -> late++;
            }
        }

        log.info("Attendance marked successfully for {} students", attendanceList.size());

        return AttendanceSummaryResponse.builder()
                .courseId(request.getCourseId())
                .courseName(course.getName())
                .date(request.getDate())
                .totalStudents(activeEnrollments.size())
                .present(present)
                .absent(absent)
                .late(late)
                .attendanceRate((double) (present + late) / activeEnrollments.size() * 100)
                .build();
    }

    /**
     * Update single attendance record
     * TEACHER who created it, within 7 days
     */
    @Transactional
    public AttendanceResponse updateAttendance(Long id, AttendanceStatus status) {
        log.info("Updating attendance ID: {} to status: {}", id, status);

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

        // Verify teacher access
        if (securityUtils.isTeacher()) {
            User currentUser = securityUtils.getCurrentUser();
            if (attendance.getEnrollment().getCourse().getTeacher() == null ||
                    !attendance.getEnrollment().getCourse().getTeacher().getUser().getId().equals(currentUser.getId())) {
                throw new BusinessException("You can only update attendance for your own courses");
            }

            // Check if within 7 days
            long daysSinceMarked = ChronoUnit.DAYS.between(attendance.getDate(), LocalDate.now());
            if (daysSinceMarked > 7) {
                throw new BusinessException("Cannot update attendance older than 7 days");
            }
        }

        attendance.setStatus(status);
        Attendance updatedAttendance = attendanceRepository.save(attendance);

        log.info("Attendance updated successfully");
        return dtoMapper.toAttendanceResponse(updatedAttendance);
    }

    /**
     * Get attendance by student
     * ADMIN, TEACHER (their courses), STUDENT (own)
     */
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByStudent(
            Long studentId, Long courseId, LocalDate startDate, LocalDate endDate) {

        log.info("Fetching attendance for student ID: {}, course: {}", studentId, courseId);

        // Verify access for students
        if (securityUtils.isStudent()) {
            List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
            if (!enrollments.isEmpty()) {
                if (!enrollments.get(0).getStudent().getUser().getId().equals(securityUtils.getCurrentUserId())) {
                    throw new BusinessException("You can only view your own attendance");
                }
            }
        }

        List<Attendance> attendanceList;

        if (courseId != null && startDate != null && endDate != null) {
            attendanceList = attendanceRepository.findByStudentIdAndCourseIdAndDateRange(
                    studentId, courseId, startDate, endDate);
        } else if (courseId != null) {
            attendanceList = attendanceRepository.findByStudentIdAndCourseId(studentId, courseId);
        } else {
            attendanceList = attendanceRepository.findByStudentId(studentId);
        }

        return attendanceList.stream()
                .map(dtoMapper::toAttendanceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get attendance by course
     * ADMIN, TEACHER (own courses)
     */
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByCourse(Long courseId, LocalDate date) {
        log.info("Fetching attendance for course ID: {}, date: {}", courseId, date);

        // Get course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Verify teacher access
        if (securityUtils.isTeacher()) {
            User currentUser = securityUtils.getCurrentUser();
            if (course.getTeacher() == null ||
                    !course.getTeacher().getUser().getId().equals(currentUser.getId())) {
                throw new BusinessException("You can only view attendance for your own courses");
            }
        }

        List<Attendance> attendanceList;
        if (date != null) {
            attendanceList = attendanceRepository.findByCourseIdAndDate(courseId, date);
        } else {
            attendanceList = attendanceRepository.findByCourseId(courseId);
        }

        return attendanceList.stream()
                .map(dtoMapper::toAttendanceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get attendance statistics for course
     * ADMIN, TEACHER (own courses)
     */
    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getAttendanceStatistics(Long courseId) {
        log.info("Fetching attendance statistics for course ID: {}", courseId);

        // Get course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Verify teacher access
        if (securityUtils.isTeacher()) {
            User currentUser = securityUtils.getCurrentUser();
            if (course.getTeacher() == null ||
                    !course.getTeacher().getUser().getId().equals(currentUser.getId())) {
                throw new BusinessException("You can only view statistics for your own courses");
            }
        }

        List<Enrollment> activeEnrollments = enrollmentRepository.findActiveEnrollmentsByCourseId(courseId);
        List<Attendance> allAttendance = attendanceRepository.findByCourseId(courseId);

        if (allAttendance.isEmpty()) {
            return AttendanceSummaryResponse.builder()
                    .courseId(courseId)
                    .courseName(course.getName())
                    .totalStudents(activeEnrollments.size())
                    .present(0)
                    .absent(0)
                    .late(0)
                    .attendanceRate(0.0)
                    .build();
        }

        int present = (int) allAttendance.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();
        int absent = (int) allAttendance.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();
        int late = (int) allAttendance.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        double attendanceRate = ((double) (present + late) / allAttendance.size()) * 100;

        return AttendanceSummaryResponse.builder()
                .courseId(courseId)
                .courseName(course.getName())
                .totalStudents(activeEnrollments.size())
                .present(present)
                .absent(absent)
                .late(late)
                .attendanceRate(attendanceRate)
                .build();
    }
}