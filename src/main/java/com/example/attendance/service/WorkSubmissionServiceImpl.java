package com.example.attendance.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.attendance.dto.HolidayRule;
import com.example.attendance.dto.WorkSubmissionRequest;
import com.example.attendance.entity.AttendanceData;
import com.example.attendance.entity.ShainData;
import com.example.attendance.repository.ShainDataMapper;
import com.example.attendance.repository.WorkTableMapper;

/**
 * 勤務確定申請における実績集計および給与計算のビジネスロジックを提供するサービス実装クラス。
 * <p>
 * 1ヶ月分の勤務データから、平日の所定日数、実出勤日数（平日・土日祝）、各種労働時間（残業・深夜・休日・遅刻）を算出し、
 * それらの集計結果を基に手当や欠勤控除を加味した最終的な月給を計算します。
 * </p>
 *
 * @author kato (or Soeda)
 */
@Service
public class WorkSubmissionServiceImpl implements WorkSubmissionService {

	/**
	 * 1ヶ月分の各種勤務集計データを一時的に保持するための、スレッドセーフな静的インナークラス。
	 * <p>
	 * サービス内の複数メソッド間で集計データをひとまとめにして受け渡すための構造体（値オブジェクト）として機能します。
	 * </p>
	 */
	private static class AttendanceSummary {
		/** 所定（平日）日数 */
		int weekdaysCount = 0;
		/** 実際の平日出勤日数（有給含む） */
		int actualWorkDays = 0;
		/** 実際の土日祝出勤日数 */
		int holidayWorkDays = 0;
		/** 確定後の有給取得日数 */
		int paidHoliday = 0;
		/** 総遅刻（不足）時間（分単位） */
		long totalBehindTime = 0;
		/** 総残業時間（分単位） */
		long totalOverMinutes = 0;
		/** 総深夜労働時間（分単位） */
		long totalLateNightMinutes = 0;
		/** 総休日労働時間（分単位） */
		long totalHolidayMinutes = 0;
	}

	@Autowired
	private ShainDataMapper shainDataMapper;
	@Autowired
	private WorkTableMapper workTableMapper;
	@Autowired
	private WorkTableService workTableService;

	/**
	 * 指定された年月の勤務実績を抽出し、各種日数の集計および総給与の計算を行い、結果を申請DTOに格納します。
	 *
	 * @param month      確定対象の月
	 * @param year       確定対象の年
	 * @param submission 算出結果（所定日数、出勤日数、有給日数、給与）をセットするためのリクエストDTO
	 * @param shain      集計対象の社員データ
	 */
	@Override
	public void dateCounts(int month, int year, WorkSubmissionRequest submission, ShainData shain) {

		// 1. 祝日リスト（振替休日含む）の取得
		List<HolidayRule> holidayList = calculateHolidays(year, month);

		// 2. 1ヶ月分の勤務実績リストを取得
		List<AttendanceData> workTimeList = workTableService.getAttendanceList(shain.getShainId(), year, month);

		// 3. 勤務実績の集計用コンテナを生成して集計実行
		AttendanceSummary summary = aggregateAttendance(year, month, workTimeList, holidayList,
				submission.getPaidHoliDay());

		// 4. 給与計算
		int totalSalary = calculateTotalSalary(shain.getShainId(), summary);

		// 5. 結果を DTO にセット
		submission.setMustDay(summary.weekdaysCount);
		submission.setAttendanceDay(summary.actualWorkDays + summary.holidayWorkDays);
		submission.setPaidHoliDay(summary.paidHoliday);
		submission.setSalary(totalSalary);
	}

	/**
	 * データベースから取得した祝日情報を基に、当月内の振替休日を補正・計算した完全な祝日リストを生成します。
	 * <p>
	 * 祝日が日曜日の場合、翌月曜日以降の最初の平日を振替休日として自動認定します。
	 * </p>
	 *
	 * @param year  対象の年
	 * @param month 対象の月
	 * @return 振替休日が追加された当月の祝日ルールリスト
	 */
	private List<HolidayRule> calculateHolidays(int year, int month) {
		List<HolidayRule> holidayList = workTableMapper.selectHoliday(month);
		Set<Integer> holidayDays = holidayList.stream()
				.map(HolidayRule::getDay)
				.filter(java.util.Objects::nonNull)
				.collect(Collectors.toSet());

		List<HolidayRule> furikaeHolidays = new ArrayList<>();

		for (HolidayRule holiday : holidayList) {
			if (holiday.getDay() != null) {
				LocalDate holidayDate = LocalDate.of(year, month, holiday.getDay());

				// 日曜日が祝日の場合、振替休日を計算
				if (holidayDate.getDayOfWeek().getValue() == 7) {
					LocalDate checkDate = holidayDate.plusDays(1);
					while (holidayDays.contains(checkDate.getDayOfMonth())) {
						checkDate = checkDate.plusDays(1);
					}

					HolidayRule furikaeRule = new HolidayRule();
					furikaeRule.setMonth(checkDate.getMonthValue());
					furikaeRule.setDay(checkDate.getDayOfMonth());

					holidayDays.add(checkDate.getDayOfMonth());
					furikaeHolidays.add(furikaeRule);
				}
			}
		}
		holidayList.addAll(furikaeHolidays);
		return holidayList;
	}

	/**
	 * 指定された日付が「土曜日」「日曜日」または「祝日リスト」に含まれる休日であるかを判定します。
	 *
	 * @param date        判定対象の日付
	 * @param holidayList 当月の祝日ルールリスト
	 * @return 休日である場合は true、平日（労働日）である場合は false
	 */
	private boolean isHoliday(LocalDate date, List<HolidayRule> holidayList) {
		int dayOfWeekNum = date.getDayOfWeek().getValue();
		if (dayOfWeekNum == 6 || dayOfWeekNum == 7) {
			return true;
		}
		return holidayList.stream()
				.anyMatch(h -> h.getDay() != null && h.getDay() == date.getDayOfMonth());
	}

	/**
	 * 1ヶ月分の勤務実績データを走査し、カレンダー上の所定日数、実際の出勤日数、および各種労働時間を集計します。
	 * <p>
	 * 備考欄が「有給」の場合は、平日出勤日数および有給加算の処理を行います。
	 * </p>
	 *
	 * @param year                対象の年
	 * @param month               対象の月
	 * @param workTimeList        1ヶ月分の勤務データリスト
	 * @param holidayList         当月の祝日ルールリスト
	 * @param initialPaidHoliday 前画面などから引き継いだ初期状態の有給取得日数
	 * @return 各種集計結果が保持された {@link AttendanceSummary} オブジェクト
	 */
	private AttendanceSummary aggregateAttendance(int year, int month, List<AttendanceData> workTimeList,
			List<HolidayRule> holidayList, int initialPaidHoliday) {
		AttendanceSummary summary = new AttendanceSummary();
		summary.paidHoliday = initialPaidHoliday;

		for (AttendanceData workData : workTimeList) {
			String note = workData.getNote() != null ? workData.getNote() : "";
			int dayNum = Integer.parseInt(workData.getWorkDay().replaceAll("[^0-9]", ""));
			LocalDate date = LocalDate.of(year, month, dayNum);

			boolean isHoliday = isHoliday(date, holidayList);

			if (!isHoliday) {
				summary.weekdaysCount++;
			}

			if ("有給".equals(note)) {
				summary.paidHoliday++;
				summary.actualWorkDays++;
				continue;
			}

			if (workData.getStartTime() != null && workData.getEndTime() != null) {
				String startStr = workData.getStartTime().toString().replaceAll("[^0-9]", "");
				String endStr = workData.getEndTime().toString().replaceAll("[^0-9]", "");

				if (startStr.isEmpty() || endStr.isEmpty() || ("0000".equals(startStr) && "0000".equals(endStr))) {
					continue;
				}

				if (!isHoliday) {
					summary.actualWorkDays++;
				} else {
					summary.holidayWorkDays++;
				}

				// 時間計算（1分単位のループ処理）
				calculateDailyMinutes(startStr, endStr, isHoliday, summary);
			}
		}
		return summary;
	}

	/**
	 * 出退勤時刻から、1日における休憩時間を除いた分単位の労働時間をループ処理で走査し、各種手当対象時間を累積します。
	 * <p>
	 * 集計対象には「残業時間（実働8時間超）」「深夜時間（22時-翌5時、46時-48時）」「休日労働時間」「不足（遅刻）時間」が含まれます。
	 * </p>
	 *
	 * @param startStr  数字のみに整形された出勤時刻文字列 (例: "0900")
	 * @param endStr    数字のみに整形された退勤時刻文字列 (例: "1800")
	 * @param isHoliday 該当レコードが休日であるかどうかのフラグ
	 * @param summary   集計時間を蓄積するためのサマリーオブジェクト
	 */
	private void calculateDailyMinutes(String startStr, String endStr, boolean isHoliday, AttendanceSummary summary) {
		int startHour = Integer.parseInt(startStr.substring(0, 2));
		int startMin = Integer.parseInt(startStr.substring(2, 4));
		int endHour = Integer.parseInt(endStr.substring(0, 2));
		int endMin = Integer.parseInt(endStr.substring(2, 4));

		int startTotalMinutes = startHour * 60 + startMin;
		int endTotalMinutes = endHour * 60 + endMin;
		int restMinutes = 60;
		int currentPassedWorkMinutes = 0;

		for (int m = startTotalMinutes; m < endTotalMinutes; m++) {
			if (m < startTotalMinutes + restMinutes) {
				continue;
			}
			currentPassedWorkMinutes++;

			boolean isOverTime = currentPassedWorkMinutes > 480;
			boolean isNightTime = (m > 0 && m < 5 * 60) || (m >= 22 * 60 && m < 29 * 60)
					|| (m >= 46 * 60 && m < 48 * 60);

			if (isHoliday)
				summary.totalHolidayMinutes++;
			if (isOverTime)
				summary.totalOverMinutes++;
			if (isNightTime)
				summary.totalLateNightMinutes++;
		}

		if (!isHoliday && currentPassedWorkMinutes < 480) {
			summary.totalBehindTime += (480 - currentPassedWorkMinutes);
		}
	}

	/**
	 * 社員の給与マスター設定および算出した各種集計時間に基づき、当月の最終支給総額（四捨五入）を計算します。
	 * <p>
	 * 計算には基本給、欠勤控除、時間外手当、深夜割増手当、休日出勤手当、および遅刻ペナルティの相殺が含まれます。
	 * </p>
	 *
	 * @param shainId 対象社員のユニークID
	 * @param summary 1ヶ月分の勤務集計データ
	 * @return 計算後の総支給額（円単位）
	 */
	private int calculateTotalSalary(int shainId, AttendanceSummary summary) {
		Map<String, Object> rate = shainDataMapper.selectSalaryById(shainId);
		int baseSalary = ((Number) rate.get("base_salary")).intValue();
		int timeCost = ((Number) rate.get("time_cost")).intValue();
		double timeSalary = 0;

		// 欠勤控除
		if (summary.weekdaysCount > summary.actualWorkDays) {
			baseSalary -= ((summary.weekdaysCount - summary.actualWorkDays) * 8 * timeCost);
		}

		double overRate = ((Number) rate.get("over_time_bonus")).doubleValue();
		double nightRate = ((Number) rate.get("late_night_bonus")).doubleValue();
		double holidayRate = ((Number) rate.get("holiday_bonus")).doubleValue();

		// 休日手当
		double holidayWorkSalary = (summary.totalHolidayMinutes / 60.0) * timeCost * holidayRate;

		if (holidayWorkSalary == 0) {
			if (summary.totalOverMinutes > summary.totalLateNightMinutes) {
				timeSalary = (summary.totalOverMinutes / 60.0) * timeCost;
			} else {
				timeSalary = (summary.totalLateNightMinutes / 60.0) * timeCost;
			}
		}

		double overTimeSalary = (summary.totalOverMinutes / 60.0) * timeCost * (overRate - 1.0);
		double lateNightSalary = (summary.totalLateNightMinutes / 60.0) * timeCost * (nightRate - 1.0);
		double behindTimePenalty = (summary.totalBehindTime / 60.0) * timeCost;

		return (int) Math.round(
				baseSalary + timeSalary + overTimeSalary + lateNightSalary + holidayWorkSalary - behindTimePenalty);
	}

}