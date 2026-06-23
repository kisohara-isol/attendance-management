package com.example.attendance.dto;

import lombok.Data;

@Data
public class WorkSubmissionRequest {

	/**
	 * 月ごとの平日日数
	 * */
	private int mustDay;

	/**
	 * 社員ごとの月の出社日数
	 * */
	private int attendanceDay;

	/**
	 * 月に有休を使った回数
	 * */
	private int paidHoliDay;

	/**
	 * 残業時間
	 * */
	private double overTime;

	/**
	 * 深夜作業時間
	 * */
	private double lateNigthTime;

	/**
	 * 休日作業時間
	 * */

	private double holidayTime;

	/**
	 * 月の給料
	 * */
	private int salary;

}
