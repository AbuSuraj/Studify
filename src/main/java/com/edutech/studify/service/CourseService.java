package com.edutech.studify.service;

import com.edutech.studify.dto.request.CreateCourseRequest;
import com.edutech.studify.dto.request.UpdateCourseRequest;
import com.edutech.studify.dto.response.CourseResponse;
import com.edutech.studify.dto.response.PageResponse;
import com.edutech.studify.dto.util.DtoMapper;
import com.edutech.studify.entity.Course;
import com.edutech.studify.entity.Department;
import com.edutech.studify.entity.Teacher;
import com.edutech.studify.exception.BusinessException;
import com.edutech.studify.exception.DuplicateResourceException;
import com.edutech.studify.exception.ResourceNotFoundException;
import com.edutech.studify.repository.CourseRepository;
import com.edutech.studify.repository.DepartmentRepository;
import com.edutech.studify.repository.TeacherRepository;
import com.edutech.studify.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * What the Course Module Must Do:
 * Admin can create courses with course code, credits, capacity
 * Anyone can view courses (students browse, teachers see assignments)
 * Track course capacity (max students, current enrollment, available seats)
 * Assign teachers to courses (one teacher per course)
 * Link courses to departments (organizational structure)
 * Search/filter courses (by department, semester, teacher, availability)
 * Prevent over-enrollment (can't enroll if course full)
 * Track enrollments (which students are in which courses)
 * Admin can update/delete courses (only if no enrollments)*/

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;
    private final DtoMapper dtoMapper;
    private final SecurityUtils securityUtils;

    /**
     * Create a new course
     * Only ADMIN can create courses
     */
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        log.info("Creating new course with code: {}", request.getCourseCode());

        // Check if course code already exists
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new DuplicateResourceException("Course", "courseCode", request.getCourseCode());
        }

        // Get department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        // Get teacher if provided
        Teacher teacher = null;
        if (request.getTeacherId() != null) {
            teacher = teacherRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", request.getTeacherId()));

            // Warning if teacher department doesn't match course department
            if (!teacher.getDepartment().getId().equals(department.getId())) {
                log.warn("Teacher department ({}) doesn't match course department ({})",
                        teacher.getDepartment().getName(), department.getName());
            }
        }

        // Create course
        Course course = Course.builder()
                .courseCode(request.getCourseCode())
                .name(request.getName())
                .description(request.getDescription())
                .credits(request.getCredits())
                .semester(request.getSemester())
                .maxCapacity(request.getMaxCapacity())
                .department(department)
                .teacher(teacher)
                .build();

        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {}", savedCourse.getId());

        return dtoMapper.toCourseResponse(savedCourse);
    }

    /**
     * Get all courses with pagination and filters
     * Accessible by all authenticated users
     */
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getAllCourses(
            Long departmentId, String semester, Long teacherId,
            int page, int size, String sortBy, String sortDir) {

        log.info("Fetching courses with filters - Dept: {}, Semester: {}, Teacher: {}",
                departmentId, semester, teacherId);

        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Course> coursePage;

        if (departmentId != null || semester != null || teacherId != null) {
            coursePage = courseRepository.searchCoursesWithFilters(null, departmentId, semester, teacherId, pageable);
        } else {
            coursePage = courseRepository.findAll(pageable);
        }

        Page<CourseResponse> responsePage = coursePage.map(dtoMapper::toCourseResponse);
        return dtoMapper.toPageResponse(responsePage);
    }

    /**
     * Search courses by name or code
     * Accessible by all authenticated users
     */
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> searchCourses(
            String search, Long departmentId, String semester, Long teacherId,
            int page, int size, String sortBy, String sortDir) {

        log.info("Searching courses with keyword: {}", search);

        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Course> coursePage = courseRepository.searchCoursesWithFilters(
                search, departmentId, semester, teacherId, pageable);

        Page<CourseResponse> responsePage = coursePage.map(dtoMapper::toCourseResponse);
        return dtoMapper.toPageResponse(responsePage);
    }

    /**
     * Get course by ID
     * Accessible by all authenticated users
     */
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        log.info("Fetching course with ID: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        return dtoMapper.toCourseResponse(course);
    }

    /**
     * Get course by course code
     */
    @Transactional(readOnly = true)
    public CourseResponse getCourseByCourseCode(String courseCode) {
        log.info("Fetching course with code: {}", courseCode);

        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "courseCode", courseCode));

        return dtoMapper.toCourseResponse(course);
    }

    /**
     * Get available courses (not full)
     */
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getAvailableCourses(int page, int size) {
        log.info("Fetching available courses");

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Course> coursePage = courseRepository.findAvailableCourses(pageable);

        Page<CourseResponse> responsePage = coursePage.map(dtoMapper::toCourseResponse);
        return dtoMapper.toPageResponse(responsePage);
    }

    /**
     * Update course
     * Only ADMIN can update courses
     */
    @Transactional
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        log.info("Updating course with ID: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Update fields if provided
        if (request.getName() != null) {
            course.setName(request.getName());
        }

        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }

        if (request.getCredits() != null) {
            course.setCredits(request.getCredits());
        }

        if (request.getSemester() != null) {
            course.setSemester(request.getSemester());
        }

        if (request.getMaxCapacity() != null) {
            // Cannot reduce max capacity below current enrollment
            int currentEnrollment = course.getEnrolledCount();
            if (request.getMaxCapacity() < currentEnrollment) {
                throw new BusinessException(
                        "Cannot reduce max capacity to " + request.getMaxCapacity() +
                                ". Current enrollment: " + currentEnrollment);
            }
            course.setMaxCapacity(request.getMaxCapacity());
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            course.setDepartment(department);
        }

        if (request.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", request.getTeacherId()));
            course.setTeacher(teacher);
        }

        Course updatedCourse = courseRepository.save(course);
        log.info("Course updated successfully with ID: {}", updatedCourse.getId());

        return dtoMapper.toCourseResponse(updatedCourse);
    }

    /**
     * Assign or change teacher for a course
     * Only ADMIN can assign teachers
     */
    @Transactional
    public CourseResponse assignTeacher(Long courseId, Long teacherId) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        log.info("Assigning teacher {} to course {}", teacherId, courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));

        // Warning if departments don't match
        if (!teacher.getDepartment().getId().equals(course.getDepartment().getId())) {
            log.warn("Teacher department ({}) doesn't match course department ({})",
                    teacher.getDepartment().getName(), course.getDepartment().getName());
        }

        course.setTeacher(teacher);
        Course updatedCourse = courseRepository.save(course);

        log.info("Teacher assigned successfully");
        return dtoMapper.toCourseResponse(updatedCourse);
    }

    /**
     * Delete course
     * Only ADMIN can delete courses
     */
    @Transactional
    public void deleteCourse(Long id) {
        // Verify admin access
        securityUtils.verifyAdminAccess();

        log.info("Deleting course with ID: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        // Check if course has enrollments
        if (course.getEnrollments() != null && !course.getEnrollments().isEmpty()) {
            throw new BusinessException(
                    "Cannot delete course with enrollments. Please unenroll all students first. " +
                            "Current enrollment count: " + course.getEnrolledCount()
            );
        }

        courseRepository.delete(course);
        log.info("Course deleted successfully with ID: {}", id);
    }
}
