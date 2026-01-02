package com.edutech.studify.dto.util;


import com.edutech.studify.dto.response.*;
import com.edutech.studify.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {

    // ============= Student Mapping =============

    public StudentResponse toStudentResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .userId(student.getUser().getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .dateOfBirth(student.getDateOfBirth())
                .address(student.getAddress())
                .department(student.getDepartment() != null ?
                        toDepartmentSummary(student.getDepartment()) : null)
                .enrollmentDate(student.getEnrollmentDate())
                .status(student.getStatus())
                .createdAt(student.getCreatedAt())
                .createdBy(student.getCreatedBy())

                .build();
    }

    private StudentResponse.DepartmentSummary toDepartmentSummary(Department department) {
        return StudentResponse.DepartmentSummary.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .build();
    }

    // ============= Teacher Mapping =============

    public TeacherResponse toTeacherResponse(Teacher teacher) {
        return TeacherResponse.builder()
                .id(teacher.getId())
                .userId(teacher.getUser().getId())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .fullName(teacher.getFullName())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .department(
                        teacher.getDepartment() == null ? null :
                                toTeacherDepartmentSummary(teacher.getDepartment())
                )
                .specialization(teacher.getSpecialization())
                .totalCourses(
                        teacher.getCourses() == null ? 0 : teacher.getCourses().size()
                )
                .createdAt(teacher.getCreatedAt())
                .createdBy(teacher.getCreatedBy())
                .lastModifiedAt(teacher.getUpdatedAt())
                .lastModifiedBy(teacher.getUpdatedBy())
                .build();
    }

    private TeacherResponse.DepartmentSummary toTeacherDepartmentSummary(Department department) {
        return TeacherResponse.DepartmentSummary.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .build();
    }

    // ============= Department Mapping =============

    public DepartmentResponse toDepartmentResponse(Department department,
                                                   Long studentCount,
                                                   Long teacherCount,
                                                   Long courseCount) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .totalStudents(studentCount)
                .totalTeachers(teacherCount)
                .totalCourses(courseCount)
                .createdAt(department.getCreatedAt())
                .createdBy(department.getCreatedBy())
                .lastModifiedAt(department.getUpdatedAt())
                .lastModifiedBy(department.getUpdatedBy())
                .build();
    }

    // ============= Course Mapping =============

    public CourseResponse toCourseResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .name(course.getName())
                .description(course.getDescription())
                .credits(course.getCredits())
                .semester(course.getSemester())
                .maxCapacity(course.getMaxCapacity())
                .enrolledCount(course.getEnrolledCount())
                .availableSeats(course.getAvailableSeats())
                .isFull(course.isFull())
                .department(
                        course.getDepartment() == null ? null :
                                toCourseDepartmentSummary(course.getDepartment())
                )
                .teacher(
                        course.getTeacher() == null ? null :
                                toTeacherSummary(course.getTeacher())
                )
                .createdAt(course.getCreatedAt())
                .createdBy(course.getCreatedBy())
                .lastModifiedAt(course.getUpdatedAt())
                .lastModifiedBy(course.getUpdatedBy())
                .build();
    }


    private CourseResponse.DepartmentSummary toCourseDepartmentSummary(Department department) {
        return CourseResponse.DepartmentSummary.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .build();
    }

    private CourseResponse.TeacherSummary toTeacherSummary(Teacher teacher) {
        return CourseResponse.TeacherSummary.builder()
                .id(teacher.getId())
                .fullName(teacher.getFullName())
                .email(teacher.getEmail())
                .build();
    }

    // ============= Enrollment Mapping =============

    public EnrollmentResponse toEnrollmentResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .student(toEnrollmentStudentSummary(enrollment.getStudent()))
                .course(toEnrollmentCourseSummary(enrollment.getCourse()))
                .enrollmentDate(enrollment.getEnrollmentDate())
                .status(enrollment.getStatus())
                .currentGrade(enrollment.getGrade() != null ?
                        enrollment.getGrade().getGrade() : null)
                .attendancePercentage(enrollment.getAttendancePercentage())
                .createdAt(enrollment.getCreatedAt())
                .createdBy(enrollment.getCreatedBy())
                .build();
    }

    private EnrollmentResponse.StudentSummary toEnrollmentStudentSummary(Student student) {
        return EnrollmentResponse.StudentSummary.builder()
                .id(student.getId())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .build();
    }

    private EnrollmentResponse.CourseSummary toEnrollmentCourseSummary(Course course) {
        return EnrollmentResponse.CourseSummary.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .name(course.getName())
                .credits(course.getCredits())
                .teacherName(course.getTeacher() != null ?
                        course.getTeacher().getFullName() : null)
                .build();
    }

    // ============= Grade Mapping =============

    public GradeResponse toGradeResponse(Grade grade) {
        return GradeResponse.builder()
                .id(grade.getId())
                .enrollmentId(grade.getEnrollment().getId())
                .student(toGradeStudentSummary(grade.getEnrollment().getStudent()))
                .course(toGradeCourseSummary(grade.getEnrollment().getCourse()))
                .grade(grade.getGrade())
                .gradePoint(grade.getGradePoint())
                .remarks(grade.getRemarks())
                .gradedDate(grade.getGradedDate())
                .gradedBy(grade.getCreatedBy())
                .createdAt(grade.getCreatedAt())
                .build();
    }

    private GradeResponse.StudentSummary toGradeStudentSummary(Student student) {
        return GradeResponse.StudentSummary.builder()
                .id(student.getId())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .build();
    }

    private GradeResponse.CourseSummary toGradeCourseSummary(Course course) {
        return GradeResponse.CourseSummary.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .name(course.getName())
                .credits(course.getCredits())
                .build();
    }

    // ============= Attendance Mapping =============

    public AttendanceResponse toAttendanceResponse(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .enrollmentId(attendance.getEnrollment().getId())
                .student(toAttendanceStudentSummary(attendance.getEnrollment().getStudent()))
                .course(toAttendanceCourseSummary(attendance.getEnrollment().getCourse()))
                .date(attendance.getDate())
                .status(attendance.getStatus())
                .createdAt(attendance.getCreatedAt())
                .createdBy(attendance.getCreatedBy())
                .build();
    }

    private AttendanceResponse.StudentSummary toAttendanceStudentSummary(Student student) {
        return AttendanceResponse.StudentSummary.builder()
                .id(student.getId())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .build();
    }

    private AttendanceResponse.CourseSummary toAttendanceCourseSummary(Course course) {
        return AttendanceResponse.CourseSummary.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .name(course.getName())
                .build();
    }

    // ============= Page Mapping =============

    public <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
