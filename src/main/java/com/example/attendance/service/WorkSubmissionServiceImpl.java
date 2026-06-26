package com.example.attendance.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.attendance.dto.WorkResultInformation;
import com.example.attendance.entity.AttendanceData;
import com.example.attendance.entity.SalaryComponentsData;
import com.example.attendance.repository.SalaryComponentsMapper;
import com.example.attendance.util.DateTimeUtil;

/**
 * worksubmissionで用いるサービスクラス
 */
@Service
public class WorkSubmissionServiceImpl implements WorkSubmissionService {

	/**テーブルsalary_componentsの操作を行うマッパー*/
	@Autowired
	SalaryComponentsMapper salaryComponentsMapper;

	/**
	 *渡された勤務データリストの中身を集計し、その期間における勤務結果を作成する。
	 *<p>給与計算においては、渡された勤務データリストの先頭要素が持つ社員IDをもとに
	 *salary_componentsテーブルを検索して、そのデータを参照する。
	 *</p>
	 *@param attendances ある社員の期間中(基本給の形状機関に一致する)における勤務データのリスト
	 *@return 渡された期間中の勤務情報を集計したWorkResultInformationオブジェクト
	 */
	@Override
	public WorkResultInformation aggregateAllAttendances(List<AttendanceData> attendances) {
		AttendanceData firstEntry = attendances.getFirst();
		int shainId = firstEntry.getShainId();
		//給与構成を取得
		var salaryComponents = salaryComponentsMapper
				.selectSalaryComponentsByShainID(shainId)
				.get();

		var result = new WorkResultInformation();
		result.setShainId(shainId);

		LocalDate firstOfMonth = LocalDate.parse(firstEntry.getWorkDay(), DateTimeUtil.SLASH_DATE_FORMAT);
		result.setYear(firstOfMonth.getYear());
		result.setMonth(firstOfMonth.getMonthValue());

		//基本給を最初にセットしておく
		result.setGrossSalary(salaryComponents.getBaseSalary());
		attendances.forEach(x -> {
			Map<String, Integer> workDataMap = analyzeOneAttendance(x, salaryComponents);
			result.setExpectedWorkingDate(result.getExpectedWorkingDate() + workDataMap.get("workDayCount"));
			result.setActualWorkingDate(result.getActualWorkingDate() + workDataMap.get("actualWorkCount"));
			result.setPtoNum(result.getPtoNum() + workDataMap.get("ptoCount"));
			result.setGrossSalary(result.getGrossSalary() + workDataMap.get("salaryDiff"));
		});
		return result;
	}

	/**
	 *一日分の勤務情報と、その社員の給与構成を受け取り、<br>
	 *給与に反映される情報のマップへと変換します。
	 *<p>帰ってくるマップは次のキーと値を持っています:
	 *<li>"workDayCount":「出勤対象日」。カレンダー上で、この日を平日として扱う場合は1、そうでなければ0</li>
	 *<li>"actualWorkCount":「出勤日数」。平日であって、かつこの日に働いている場合は1、そうでなければ0</li>
	 *<li>"ptoCount":「有給日数」。平日であって、この日に有給を撮っている場合は1、そうでなければ0</li>
	 *<li>"salaryDiff":「総支給額」への差分。基本給は支払われるものと仮定したうえで、<br>
	 *残業・深夜労働・休日出勤による増分か、
	 *遅刻等で既定の時間分勤務ができていない分の減算分の値が入る。</li>
	 *</p>
	 *
	 *@param attendance 一日分の勤務情報
	 *@param salaryComponents 給与構成の情報
	 *@return "workDayCount","actualWorkCount","ptoCount","salaryDiff"の四つのキーを持つマップ
	 *@throws IllegalArgumentException 勤務情報と給与構成が、別々の社員IDに結びついている場合
	 */
	@Override
	public Map<String, Integer> analyzeOneAttendance(AttendanceData attendance,
			SalaryComponentsData salaryComponents) {
		//二つのデータが同じ社員のものか確認
		if (attendance.getShainId() != salaryComponents.getShainId()) {
			throw new IllegalArgumentException("勤務記録と給与構成の社員IDが一致しません");
		}

		//時間単価
		final double TIME_COST = salaryComponents.getTimeCost();

		//定時勤務の勤務時間(分)
		final int REGULAR_WORK_MINUTES = AttendanceData.REGULAR_WORK_TIME;

		//結果
		Map<String, Integer> result = new HashMap<>();
		result.put("workDayCount", 0);
		result.put("actualWorkCount", 0);
		result.put("ptoCount", 0);
		result.put("salaryDiff", 0);

		boolean isHoliday = false;
		//カレンダー上で休日・祝日か否かの判定
		if (attendance.getDayOfWeek().matches("土|日") || attendance.getHoliday() != null) {
			if (attendance.isBreakDay()) {
				//カレンダー上休みの日で勤務がなければresultは何も変更されない
				return result;
			}
			isHoliday = true;
		} else {
			//平日なら勤務対象日のカウントを1増加
			result.replace("workDayCount", 1);

			if (attendance.isBreakDay()) {
				//勤務が休みであった場合
				if ("有給".equals(attendance.getNote())) {
					//有給を取ったならカウントを増加してresultを返す
					result.replace("ptoCount", 1);
				} else {
					//ただの休みの場合、給与差分は負の8時間分
					int dailyWage = salaryCalculation(TIME_COST, REGULAR_WORK_MINUTES, 1);
					result.replace("salaryDiff", -dailyWage);
				}
				return result;
			}
			//平日で勤務があるなら実勤務日のカウントを1増加
			result.replace("actualWorkCount", 1);
		}

		//勤務の最初と最後の時間を取得
		LocalDate workDay = LocalDate.parse(attendance.getWorkDay(), DateTimeUtil.SLASH_DATE_FORMAT);
		LocalDateTime startTime = DateTimeUtil.correctOver24Hour(
				workDay, attendance.getStartTime(), AttendanceData.TIME_FORMAT);
		LocalDateTime endTime = DateTimeUtil.correctOver24Hour(
				workDay, attendance.getEndTime(), AttendanceData.TIME_FORMAT);

		//勤務時間
		var duration = Duration.between(startTime, endTime);
		int workMinutes = (int) (duration.toMinutesPart() + duration.toHours() * 60);

		//定時勤務との勤務時間差分を算出
		int additionalWorkMinutes = workMinutes - REGULAR_WORK_MINUTES;

		//深夜残業の対象になる時間を0時0分からの経過分数で表現

		final int EARLY_BORDER = 5 * 60; //(0時から)5時
		final int LATE_BORDER = 22 * 60; //22時(から24時)
		int[][] workedPeriod = DateTimeUtil.elapsedMinutesFromMidnight(startTime, endTime);
		//夜勤の分数
		int lateNightWorkedMinutes = 0;

		for (int[] dateWork : workedPeriod) {
			int start = dateWork[0];
			int end = dateWork[1];

			if (start < EARLY_BORDER) {
				//startから(5時またはendの早いほう)まで働いた
				lateNightWorkedMinutes += Math.min(EARLY_BORDER, end) - start;
			}
			if (end >= LATE_BORDER) {
				//(22時またはstartの遅いほう)からendまで働いた
				lateNightWorkedMinutes += end - Math.max(LATE_BORDER, start);
			}
		}

		//この日計算された給与の増減分
		int salaryDiff = 0;
		if (isHoliday) {
			//休日なら、まず勤務全体から休日手当の増分を加算
			salaryDiff += salaryCalculation(TIME_COST, workMinutes, (salaryComponents.getHolidayBonus()) - 1);
			if (additionalWorkMinutes > 0) {
				//休日出勤は、勤務時間が定時分未満でも減算をしない
				salaryDiff += salaryCalculation(TIME_COST, additionalWorkMinutes, salaryComponents.getOverTimeBonus());
			}
		} else {
			salaryDiff += salaryCalculation(TIME_COST, additionalWorkMinutes,
					(additionalWorkMinutes > 0)
							? salaryComponents.getOverTimeBonus() //残業であれば超過分給与に残業倍率を乗せる
							: 1); //遅刻による不足であれば減算する給与は1倍で計算
		}
		//深夜代金の増分を加算
		salaryDiff += salaryCalculation(TIME_COST, lateNightWorkedMinutes, salaryComponents.getLateNightBonus() - 1);
		result.replace("salaryDiff", salaryDiff);
		return result;
	}

	/**
	 * 引数に受け取った時間単価、分、倍率から、同一倍率下の総給与を計算する。<br>
	 * 給与に小数部分が発生する場合は切り上げる
	 * @param timeCost 時間単価
	 * @param Minutes 勤務時間(分)
	 * @param rate 給与の倍率
	 * @return 与えられた時間で発生した給与 
	 */
	private static int salaryCalculation(double timeCost, int Minutes, double rate) {
		long result = Math.round(timeCost * Minutes / 60 * rate);
		return (int) result;
	}

}
