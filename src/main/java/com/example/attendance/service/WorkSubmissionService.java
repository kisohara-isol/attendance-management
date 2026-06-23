package com.example.attendance.service;

import org.springframework.stereotype.Service;

import com.example.attendance.dto.WorkSubmissionRequest;
import com.example.attendance.entity.ShainData;

@Service
public interface WorkSubmissionService {

	void dateCounts(int workYear, int workMonth, WorkSubmissionRequest submission,ShainData shain);
}
