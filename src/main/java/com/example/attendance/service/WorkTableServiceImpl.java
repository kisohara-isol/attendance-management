package com.example.attendance.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.example.attendance.entity.AttendanceData;
import com.example.attendance.repository.WorkTableMapper;
import com.example.attendance.util.LogUtil;

/**
 * 勤務表に関する業務ロジックを提供するサービス実装クラスです。
 */
@Service
public class WorkTableServiceImpl implements WorkTableService {

	@Autowired
	private WorkTableMapper workTableMapper;

	@Override
	public List<AttendanceData> getAttendanceList(int shainId, int year, int month) {

		LocalDate start = LocalDate.of(year, month, 1);
		LocalDate end = LocalDate.of(year, month, 1).plusMonths(1);

		List<AttendanceData> dbList = null;

		try {
			dbList = workTableMapper.selectAttendance(shainId, start, end);
			LogUtil.info("データベース[attendance_table]にアクセスしました。");
		} catch (DataAccessException e) {
			LogUtil.error("E10001: データベースアクセスに失敗しました。", e);
			return new ArrayList<AttendanceData>();
		}

		List<AttendanceData> processedList = new ArrayList<AttendanceData>();

		for (AttendanceData ad : dbList) {

			if (ad.getWorkDay() != null) {
				String rawDay = ad.getWorkDay();
				try {
					LocalDate parsedDate = LocalDate.parse(rawDay);
					DayOfWeek dw = parsedDate.getDayOfWeek();
					ad.setDayOfWeek(dw.getDisplayName(TextStyle.SHORT, Locale.JAPAN));
					ad.setWorkDay(String.valueOf(parsedDate.getDayOfMonth()));
				} catch (Exception e) {
					String cleanDay = rawDay.replaceAll("[^0-9]", "");
					if (cleanDay.length() >= 2) {
						ad.setWorkDay(String.valueOf(Integer.parseInt(cleanDay.substring(cleanDay.length() - 2))));
					}
				}
			}

			String startTimeStr = ad.getStartTime() != null ? ad.getStartTime().trim() : "00:00:00";
			String endTimeStr = ad.getEndTime() != null ? ad.getEndTime().trim() : "00:00:00";

			int startHour = 0, startMin = 0, endHour = 0, endMin = 0;

			if (startTimeStr.contains(":")) {
				String[] sParts = startTimeStr.split(":");
				startHour = Integer.parseInt(sParts[0].trim());
				startMin = Integer.parseInt(sParts[1].trim());
			}
			if (endTimeStr.contains(":")) {
				String[] eParts = endTimeStr.split(":");
				endHour = Integer.parseInt(eParts[0].trim());
				endMin = Integer.parseInt(eParts[1].trim());
			}

			int startTotalMinutes = startHour * 60 + startMin;
			int endTotalMinutes = endHour * 60 + endMin;

			// 💡 【超安全化】確実に「退勤 - 出勤」で労働分数を算出
			long minutes = (long) endTotalMinutes - (long) startTotalMinutes;
			ad.setMinutes(minutes);

			// 休み判定
			if (startTotalMinutes == 0 && endTotalMinutes == 0) {
				ad.setBreakDay(true);
			}

			// 出勤時間の表記維持
			if (startHour >= 24) {
				ad.setStartTime(String.format("%d:%02d", startHour, startMin));
			} else {
				ad.setStartTime(String.format("%02d:%02d", startHour, startMin));
			}

			// 退勤時間の表記維持
			if (endHour >= 24) {
				ad.setEndTime(String.format("%d:%02d", endHour, endMin));
			} else {
				ad.setEndTime(String.format("%02d:%02d", endHour, endMin));
			}

			// 💡 【残業時間計算のバグを完全駆除】
			// 実労働が9時間（540分＝定時8時間＋休憩1時間）を超えている場合、絶対にここを通します
			if (minutes > 540) {
				long over = minutes - 540;
				int overHour = (int) (over / 60);
				int overMinutes = (int) (over % 60);
				// Thymeleafが解釈しやすいよう、余計な空白を入れず「8:00」の形でガチッと固定
				ad.setOverTime(String.format("%d:%02d", overHour, overMinutes));
			} else {
				ad.setOverTime("");
			}

			processedList.add(ad);
		}

		return processedList;
	}
}