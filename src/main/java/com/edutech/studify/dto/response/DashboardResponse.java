package com.edutech.studify.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private Long totalStudents;
    private Long activeStudents;
    private Long inactiveStudents;
    private Long graduatedStudents;

    private Long totalTeachers;

    private Long totalCourses;
    private Long activeCourses;

    private Long totalDepartments;

    private Long totalEnrollments;
    private Long activeEnrollments;

    private BigDecimal averageGPA;
    private Double averageAttendanceRate;
}