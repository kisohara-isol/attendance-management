package com.example.attendance.dto;

import lombok.Data;

@Data
public class HolidayRule {

	/**
	 * 休日ID
	 * */
	private Integer id;
	/**
	 * 祝日名
	 * */
	private String holidayName; // MyBatisが自動でスネークケース(holiday_name)からキャメルケースに変換してくれます
	/**
	 * 月
	 * */
	private int month;
	/**
	 * 日
	 * */
	private Integer day;// NULLが入る可能性があるので、intではなくラッパークラスのIntegerにします
	/**
	 * 何週目
	 * */
	private Integer weekNumber;
	/**
	 * 週何日目
	 * */
	private Integer dayOfWeek;

}
