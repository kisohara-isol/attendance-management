package com.example.attendance.service;

import java.sql.Date;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.example.attendance.entity.AttendanceData;
import com.example.attendance.repository.WorkTableMapper;
import com.example.attendance.util.LogUtil;

/**
 * 勤務表に関する業務ロジックを提供するサービス実装クラスです。
 * <p>
 * データベースから取得した生の勤怠データを基に、画面表示や集計に適した形式
 * （曜日、フォーマット済み日付、実労働時間、残業時間など）への加工・計算を行います。
 * </p>
 *
 * @author Hagiwara
 */
@Service
public class WorkTableServiceImpl implements WorkTableService {

	/** 勤怠テーブルにアクセスするためのマッパーインターフェース */
	@Autowired
	WorkTableMapper workTableMapper;

	private Map<String, DayOfWeek> dayMap = Map.of("月", DayOfWeek.MONDAY, "火", DayOfWeek.TUESDAY, "水",
			DayOfWeek.WEDNESDAY, "木", DayOfWeek.THURSDAY, "金", DayOfWeek.FRIDAY, "土", DayOfWeek.SATURDAY, "日",
			DayOfWeek.SUNDAY);

	/**
	 * 指定された社員および年月に紐づく勤怠明細一覧を取得・加工します。
	 * <p>
	 * データベースから取得した生の勤怠データを、曜日、フォーマット済みの年月日、 実労働時間、残業時間などを計算・設定したオブジェクトのリストに変換します。
	 * </p>
	 *
	 * @author Hagiwara
	 *
	 * @param shainId 対象の社員ID
	 * @param year    対象の年 (例: 2026)
	 * @param month   対象の月 (1〜12)
	 * @return 加工済みの勤怠明細データ {@link AttendanceData} のリスト
	 * @throws java.time.DateTimeException 年月の指定が不正な場合に発生する可能性があります
	 */
	@Override
	public List<AttendanceData> getAttendanceList(int shainId, int year, int month) {
		// TODO 自動生成されたメソッド・スタブ

		// 月の1日目、来月の1日目を作る
		LocalDate start = LocalDate.of(year, month, 1);
		LocalDate end = LocalDate.of(year, month, 1).plusMonths(1);

		List<Map<String, Object>> result = null;
		
		//データベースにアクセスする際のエラーのチェック
		try {
			result = (List<Map<String, Object>>) workTableMapper.selectAttendance(shainId, start, end);
			LogUtil.info("データベース[attendance_table]にアクセスしました。");
		} catch (DataAccessException e) {
			LogUtil.error("E10001");
			//空のリストを返す
			return new ArrayList<AttendanceData>();
		}

		List<AttendanceData> list = new ArrayList<AttendanceData>();
		for (Map<String, Object> low : result) {
			//インスタンスに格納
			AttendanceData ad = new AttendanceData();
			
			ad.setShainId((int) low.get("shain_id"));
			
			Date workDay = (Date) low.get("work_day");
			Time st = (Time) low.get("start_time");
			String et = (String) low.get("end_time_str");
			
			// 曜日取得・格納
			DayOfWeek dw = workDay.toLocalDate().getDayOfWeek();
			String dayOfWeek = dw.getDisplayName(TextStyle.SHORT, Locale.JAPAN);
			ad.setDayOfWeek(dayOfWeek);
			
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd");
			String wd = workDay.toLocalDate().format(fmt);
			
			// 出勤日・備考を格納
			ad.setWorkDay(wd);
			ad.setNote((String) low.get("note"));
			
			LocalTime startTime = st.toLocalTime();
			// String型を分解
			int endHour = Integer.parseInt(et.split(":")[0]);
			int endMinutes = Integer.parseInt(et.split(":")[1]);

			long minutes = 0;
			// 退勤時間が24時間を超えた時間だった場合
			if (endHour >= 24) {
				LocalDateTime startDateTime = LocalDateTime.of(workDay.toLocalDate(), startTime);
				LocalDateTime endDateTime = LocalDateTime.of(workDay.toLocalDate().plusDays(1),
						LocalTime.of(endHour - 24, endMinutes));
				// 勤務時間(分)を格納
				minutes = ChronoUnit.MINUTES.between(startDateTime, endDateTime);
				ad.setMinutes(minutes);
			} else {
				// 勤務時間(分)を格納
				minutes = ChronoUnit.MINUTES.between(startTime, LocalTime.of(endHour, endMinutes));
				ad.setMinutes(minutes);
			}
			// 残業時間を格納
			if(minutes > 480) {
				long overMinutes = minutes - 480;
				ad.setOverMinutes(overMinutes);
				// 残業時間(勤務表表記用)を格納
				long overHour = overMinutes / 60;
				long overMin = overMinutes % 60;
				ad.setOverTime(overHour + ":" + String.format("%02d", overMin));
			}
			
			// 出勤時間・退勤時間が両方00:00の時は休み
			LocalTime breakTime = LocalTime.of(00, 00);
			if (startTime.equals(breakTime) && LocalTime.of(endHour, endMinutes).equals(breakTime)) {
				ad.setBreakDay(true);
			}

			ad.setStartTime(startTime);
			ad.setEndTime(et);

			// 登録しているかどうか
			ad.setRegistration(true);

			list.add(ad);
		}

		return list;
	}

	/**
	 * 実際の出勤日と出勤していない日を合わせたカレンダーを作るメソッド
	 * 
	 * @param shainId 社員ID
	 * @param year    カレンダーを作る年
	 * @param month   カレンダーを作る月
	 * @return 出勤日と休みの日が混ざったリスト
	 */
	@Override
	public List<AttendanceData> getCalendar(int shainId, int year, int month) {
		List<AttendanceData> workList = getAttendanceList(shainId, year, month);

		// 実際に反映させる用の勤務表カレンダーを用意
		List<AttendanceData> workCalendar = new ArrayList<AttendanceData>();

		// 勤務していなくても日にちを表示する
		// 月の日数を取得
		YearMonth yearMonth = YearMonth.of(year, month);
		int dayOfMonth = yearMonth.lengthOfMonth();

		// 反映させる勤務カレンダーを作る
		for (int day = 1; day <= dayOfMonth; day++) {
			// LocalDateオブジェクトをつくる
			LocalDate date = LocalDate.of(year, month, day);

			// yyyy/MM/dd の形式にフォーマットする
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
			String formattedDate = date.format(formatter);

			boolean actualFlag = false; // 登録されているかどうかフラッグ

			for (AttendanceData ad : workList) {
				String actualWorkDay = ad.getWorkDay();
				// 実際に勤務している日程だった場合カレンダーに入れる
				if (formattedDate.equals(actualWorkDay)) {
					if (ad.isBreakDay() == false) {
						// 出勤時間・退勤時間が両方00:00の時は休み
						String et = ad.getEndTime();
						int endHour = Integer.parseInt(et.split(":")[0]);
						int endMinutes = Integer.parseInt(et.split(":")[1]);
						LocalTime breakTime = LocalTime.of(00, 00);
						if (ad.getStartTime().equals(breakTime)
								&& LocalTime.of(endHour, endMinutes).equals(breakTime)) {
							ad.setBreakDay(true);
						}
						DayOfWeek dw = dayMap.get(ad.getDayOfWeek());
						String holidayName = Holiday.judgeHoliday(date);
						// 土日祝の場合休みにする
						if (dw == DayOfWeek.SATURDAY || dw == DayOfWeek.SUNDAY) {
							ad.setBreakDay(true);
							// 祝日の時
						} else if (holidayName != null) {
							if(ad.getNote() == null) {
								ad.setNote(holidayName); // 備考欄に何も書いてなかったら祝日の名前を入れる
							}
							ad.setBreakDay(true);
						}
					}
					workCalendar.add(ad);
					actualFlag = true;
					break;
				}
			}

			// 勤務していない日程だった場合、空白の日程をカレンダーに入れる
			if (actualFlag == false) {
				AttendanceData ad = new AttendanceData();

				ad.setWorkDay(formattedDate);

				// 曜日
				DayOfWeek dw = date.getDayOfWeek();
				String dayOfWeek = dw.getDisplayName(TextStyle.SHORT, Locale.JAPAN);
				ad.setDayOfWeek(dayOfWeek);

				String holidayName = Holiday.judgeHoliday(date);

				// 土日の時
				if (dw == DayOfWeek.SATURDAY || dw == DayOfWeek.SUNDAY) {
					ad.setBreakDay(true);
					// 祝日の時
				} else if (holidayName != null) {
					// 備考に祝日の名前をセットする
					ad.setNote(holidayName);
					ad.setBreakDay(true);
				}

				// 登録していない
				ad.setRegistration(false);

				workCalendar.add(ad);
			}
		}

		return workCalendar;
	}

}
