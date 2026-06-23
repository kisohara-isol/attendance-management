package com.example.attendance.entity;

import lombok.Data;
/**
 * 社員の給与計算に関する情報を保持するエンティティクラスです。
 * <p>
 * このクラスは、基本給や時間単価、および各種手当（残業、深夜、休日）の
 * 倍率または金額を管理するために使用されます。
 * </p>
 */
@Data
public class SalaryData {
	
	/** 社員ID */
	private int shainId;
	
	/** 基本給 */
	private int baseSalary;
	
	/** 時間単価 */
	private int timeCost;
	
	/** 残業手当 */
	private double overTimeBonus;
	
	/** 深夜手当 */
	private double lateNightBonus;
	
	/** 休日手当 */
	private double holidayBonus;
}
