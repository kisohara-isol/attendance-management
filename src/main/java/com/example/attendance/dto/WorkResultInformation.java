package com.example.attendance.dto;

import lombok.Data;

/**
 * work_submissionのページで用いるひと月分の出勤結果を保持するためのクラス
 */
@Data
public class WorkResultInformation {
	/**社員id*/
	int shainId;
	/**対象年*/
	int year;
	/**対象月*/
	int month;
	/**その月の出勤対象日数(平日の数)*/
	int expectedWorkingDate;
	/**実際に出勤した日数*/
	int actualWorkingDate;
	/**有給取得数*/
	int ptoNum;
	/**総支給額*/
	int grossSalary;
}
