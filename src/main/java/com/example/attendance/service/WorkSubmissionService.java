package com.example.attendance.service;

import java.util.List;
import java.util.Map;

import com.example.attendance.dto.WorkResultInformation;
import com.example.attendance.entity.AttendanceData;
import com.example.attendance.entity.SalaryComponentsData;

public interface WorkSubmissionService {
	WorkResultInformation aggregateAllAttendances(List<AttendanceData> attendances);

	Map<String, Integer> analyzeOneAttendance(AttendanceData attendance, SalaryComponentsData salaryComponents);
}
