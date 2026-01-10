package com.edutech.studify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSummaryResponse {

    private Long courseId;
    private String courseName;
    private LocalDate date;
    private Integer totalStudents;
    private Integer present;
    private Integer absent;
    private Integer late;
    private Double attendanceRate;
}