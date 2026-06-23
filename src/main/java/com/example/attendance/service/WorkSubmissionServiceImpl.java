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

@Service // ★Serviceクラスにはこれを忘れずに！
public class WorkSubmissionServiceImpl implements WorkSubmissionService {

	@Autowired
	private ShainDataMapper shainDataMapper;
	@Autowired
	private WorkTableMapper workTableMapper;
	@Autowired
	private WorkTableService workTableService;

	@Override
	public void dateCounts(int month, int year, WorkSubmissionRequest submission, ShainData shain) {

		// 1. 祝日リストの取得（DBから当月の祝日を取得）
		List<HolidayRule> holidayList = workTableMapper.selectHoliday(month);

		// 検索を高速化＆判定しやすくするため、当月の「祝日の『日（Integer）』」のセットを作る
		Set<Integer> holidayDays = holidayList.stream()
				.map(HolidayRule::getDay)
				.filter(java.util.Objects::nonNull)
				.collect(Collectors.toSet());

		List<HolidayRule> furikaeHolidays = new ArrayList<>();

		// 💡 振替休日の計算ロジック
		for (HolidayRule holiday : holidayList) {
			if (holiday.getDay() != null) {
				LocalDate holidayDate = LocalDate.of(year, month, holiday.getDay());

				// もし祝日が「日曜日 (値: 7)」だった場合
				if (holidayDate.getDayOfWeek().getValue() == 7) {

					// 翌日（月曜日）からスタートして、祝日じゃない日を探す
					LocalDate checkDate = holidayDate.plusDays(1);

					// 💡 ループ条件：チェックした日が「すでにDBにある祝日リスト」に含まれている間は、さらに翌日へ進む
					// ※当月内のループを想定（月をまたぐ特殊なケースは日本の祝日法上、GWでも発生しません）
					while (holidayDays.contains(checkDate.getDayOfMonth())) {
						checkDate = checkDate.plusDays(1);
					}

					// ループを抜けた日（＝祝日ではない最初の平日）が振替休日になる！
					HolidayRule furikaeRule = new HolidayRule();
					furikaeRule.setMonth(checkDate.getMonthValue());
					furikaeRule.setDay(checkDate.getDayOfMonth());

					// 重複して同じ日が振替休日にならないように、今回の判定用セットにも追加しておく
					holidayDays.add(checkDate.getDayOfMonth());
					furikaeHolidays.add(furikaeRule);
				}
			}
		}
		// 本物の祝日リストに、計算した振替休日を合流させる
		holidayList.addAll(furikaeHolidays);

		// 2. 1ヶ月分の勤務実績リストを取得
		List<AttendanceData> workTimeList = workTableService.getAttendanceList(shain.getShainId(), year, month);

		// カウント・時間蓄積用の変数（分単位）
		int weekdaysCount = 0; // 所定（平日）日数
		int actualWorkDays = 0; // 実際の「平日」出勤日数 💡平日のみカウントに変更
		int holidayWorkDays = 0; // ⭕【追加】実際の「土日祝」出勤日数
		long totalBehindTime = 0; // 総遅刻（不足）時間
		long totalOverMinutes = 0; // 総残業時間
		long totalLateNightMinutes = 0; // 総深夜時間
		long totalHolidayMinutes = 0; // 総休日労働時間
		int paidHoliday = submission.getPaidHoliDay(); // 有給日数

		// 3. 1日ずつループして判定
		for (AttendanceData workData : workTimeList) {

			String note = workData.getNote() != null ? workData.getNote() : "";
			int dayNum = Integer.parseInt(workData.getWorkDay().replaceAll("[^0-9]", ""));
			LocalDate date = LocalDate.of(year, month, dayNum);

			// --- 土日祝の判定 ---
			boolean isHoliday = false;
			int dayOfWeekNum = date.getDayOfWeek().getValue();
			if (dayOfWeekNum == 6 || dayOfWeekNum == 7) {
				isHoliday = true;
			}
			if (!isHoliday) {
				for (HolidayRule holiday : holidayList) {
					if (holiday.getDay() != null && holiday.getDay() == dayNum) {
						isHoliday = true;
						break;
					}
				}
			}

			// 💡 【仕様変更】平日の所定日数は、純粋にカレンダー上の平日のみをカウント
			if (!isHoliday) {
				weekdaysCount++;
			}

			// 有給の判定
			if ("有給".equals(note)) {
				paidHoliday++;
				actualWorkDays++; // 有給は平日の出勤扱いに含める
				continue;
			}

			// 時間の取り出しと計算
			if (workData.getStartTime() != null && workData.getEndTime() != null) {

				String startStr = workData.getStartTime().toString().replaceAll("[^0-9]", "");
				String endStr = workData.getEndTime().toString().replaceAll("[^0-9]", "");

				if (startStr.isEmpty() || endStr.isEmpty() || ("0000".equals(startStr) && "0000".equals(endStr))) {
					continue;
				}

				// 💡 【仕様変更】出勤日数のカウントを平日と休日で分ける
				if (!isHoliday) {
					actualWorkDays++; // 平日の出勤日数
				} else {
					holidayWorkDays++; // 土日祝の出勤日数
				}

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
					boolean isNightTime = (m >= 22 * 60 && m < 29 * 60) || (m >= 46 * 60 && m < 53 * 60);

					if (isHoliday) {
						totalHolidayMinutes++;
					}
					if (isOverTime) {
						totalOverMinutes++;
					}
					if (isNightTime) {
						totalLateNightMinutes++;
					}
				}

				if (!isHoliday && currentPassedWorkMinutes < 480) {
					totalBehindTime += (480 - currentPassedWorkMinutes);
				}
			}
		}

		/**
		 * 4. 給与計算
		 */
		Map<String, Object> rate = shainDataMapper.selectSalaryById(shain.getShainId());
		int baseSalary = ((Number) rate.get("base_salary")).intValue();
		int timeCost = ((Number) rate.get("time_cost")).intValue();
		//時間外労働分の時給（手当は含んでいない）
		double timeSalary = 0;

		// 💡 ① 【平日分だけで基本給の判定を行う】
		// 純粋な平日の所定日数と、平日の出勤日数を比較して欠勤控除を計算
		if (weekdaysCount > actualWorkDays) {
			baseSalary = baseSalary - ((weekdaysCount - actualWorkDays) * 8 * timeCost);
		}

		double overRate = ((Number) rate.get("over_time_bonus")).doubleValue();
		double nightRate = ((Number) rate.get("late_night_bonus")).doubleValue();
		double holidayRate = ((Number) rate.get("holiday_bonus")).doubleValue();

		// 💡 ③ 【休みの日の給料は手当含め時給ですべて計算】
		// 基本給(baseSalary)には土日祝の等倍分すら入っていないため、
		// 休日労働時間に対して「時給 × 休日割増率（例: 1.35）」を丸ごと掛け算して支給します。
		double holidayWorkSalary = (totalHolidayMinutes / 60.0) * timeCost * holidayRate;

		//土日の場合は時給計算されているため下記の計算は省く
		if (holidayWorkSalary == 0) {

			// 時間外労働の時給計算（残業・深夜時間が長い物をベースに時給を算出）
			if (totalOverMinutes > totalLateNightMinutes) {
				timeSalary = (totalOverMinutes / 60.0) * timeCost;
			} else if (totalLateNightMinutes <= totalOverMinutes) {
				timeSalary = (totalLateNightMinutes / 60.0) * timeCost;
			}
		}
		//残業手当・深夜手当の算出

		double overTimeSalary = (totalOverMinutes / 60.0) * timeCost * (overRate - 1.0);

		double lateNightSalary = (totalLateNightMinutes / 60.0) * timeCost * (nightRate - 1.0);

		// ④ 遅刻ペナルティ
		double behindTimePenalty = (totalBehindTime / 60.0) * timeCost;

		// 【総給与の算出】
		int totalSalary = (int) Math.round(
				baseSalary + timeSalary + overTimeSalary + lateNightSalary + holidayWorkSalary - behindTimePenalty);

		System.out.println(overTimeSalary);
		System.out.println(lateNightSalary);
		System.out.println(holidayWorkSalary);
		System.out.println(behindTimePenalty);
		System.out.println(totalSalary);
		System.out.println(timeSalary);

		// 各種計算結果を DTO にセット
		submission.setMustDay(weekdaysCount); // 平日の所定日数

		// 画面に返す出勤日数は、「平日の出勤日数 ＋ 土日祝の出勤日数」の合計値にする
		submission.setAttendanceDay(actualWorkDays + holidayWorkDays);

		submission.setPaidHoliDay(paidHoliday);
		submission.setSalary(totalSalary);
	}
}