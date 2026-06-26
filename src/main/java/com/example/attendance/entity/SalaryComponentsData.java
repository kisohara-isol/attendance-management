package com.example.attendance.entity;

import lombok.Data;

/**
 * 給与構成(salary_components)テーブルの情報を1レコード分保持するクラス
 */
@Data
public class SalaryComponentsData {
	/**社員ID*/
	int shainId;
	/**基本給*/
	int baseSalary;
	/**時間単価 超過または不足した勤務時間分の金額差分を算出するために用いる*/
	int timeCost;
	/**残業代の倍率*/
	double overTimeBonus;
	/**深夜手当の倍率*/
	double lateNightBonus;
	/**休日手当の倍率*/
	double holidayBonus;
}
