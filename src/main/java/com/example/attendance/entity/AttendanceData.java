package com.example.attendance.entity;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import com.example.attendance.util.DateTimeUtil;

import lombok.Data;

/**
 * 1日ごとの勤務実績データを保持するエンティティクラス。
 * <p>
 * <b>【ShainData との違い】</b><br>
 * ・{@code ShainData}: 社員の「マスター情報」（ログインID、パスワード、氏名など）を管理し、
 * 主に認証やセッション保持に使用されます。<br>
 * ・{@code AttendanceData}: その社員が「いつ、何時間働いたか」という
 * 「日々の勤務実績情報」（出退勤時刻、勤務分数、残業時間など）を管理します。<br>
 * つまり、{@code ShainData}（誰が）に紐づく、日々の活動を記録するのがこの {@code AttendanceData} です。
 * </p>
 *
 * @author Hagiwara
 */
@Data
public class AttendanceData {
	/** 定時の勤務時間(分)*/
	public static final int REGULAR_WORK_TIME = 480;
	
	/** 社員を一意に識別するための社員ID（ShainDataのIDと紐づきます） */
	private int shainId;

	/** 出勤日（フォーマット済みの文字列 例: "2026/06/02"） */
	private String workDay;

	/** 出勤時刻（時:分） */
	private String startTime;

	/** 退勤時刻（時:分） */
	private String endTime;

	/** 当日の総勤務時間（分数単位） */
	private long minutes;

	/** 勤務に関する備考（有給申請や遅刻理由など） */
	private String note;

	/** 出勤または休みのフラグ（true: 休み、false: 出勤日） */
	private boolean breakDay;

	/** 定時を超過した残業時間（フォーマット済みの文字列 例: " 1:30"） */
	private String overTime;

	/** 出勤日の曜日（例: "月", "火"） */
	private String dayOfWeek;
	
	/**祝日の場合、その種類*/
	private String holiday;
	
	/**
	 * 受け取った勤務実績データに対して、日付と時間の書式変更や<br>
	 * 休日判定、勤務時間と残業時間の設定などのセットアップを行う。
	 * @param attendanceData 対象の勤務実績データ
	 * @return セットアップされた勤務実績データ
	 */
	public static AttendanceData setUpAttendanceData(AttendanceData attendanceData) {
		//時間のフォーマット LocalTime用と正規表現用が1:1で対応
		final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");//LocalTime用フォーマット
		final Pattern timePattern = Pattern.compile("^\\d{2,}:\\d{2}");//文字列用正規表現 時間部分が3桁以上も許容

		var workDay = LocalDate.parse(attendanceData.getWorkDay());

		//日付の書式を変更・格納
		attendanceData.setWorkDay(workDay.format(DateTimeUtil.SLASH_DATE_FORMAT));
		//曜日を格納
		attendanceData.setDayOfWeek(workDay.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPAN));

		//時間の書式を変更・格納
		//変更用ロジック
		UnaryOperator<String> formatWorkTime = x -> {
			var matcher = timePattern.matcher(x);
			matcher.find();
			//timePatternにマッチした部分のみを返却
			return matcher.group();
		};
		var formattedStartTimeString = formatWorkTime.apply(attendanceData.getStartTime());
		var formattedEndTimeString = formatWorkTime.apply(attendanceData.getEndTime());
		attendanceData.setStartTime(formattedStartTimeString);
		attendanceData.setEndTime(formattedEndTimeString);

		//出勤時間と退勤時間に日付情報を結合する
		var startDateTime = DateTimeUtil
				.correctOver24Hour(workDay, formattedStartTimeString, timeFormat);
		var endDateTime = DateTimeUtil
				.correctOver24Hour(workDay, formattedEndTimeString, timeFormat);
		//休みの判別基準は「00時00分」
		var breakExpression = LocalDateTime.of(workDay, LocalTime.of(0, 0));

		if (breakExpression.equals(startDateTime) && breakExpression.equals(endDateTime)) {
			//出退勤時間の両方が"00:00"の時は休みを設定
			attendanceData.setBreakDay(true);
		} else {
			long workMinutes = ChronoUnit.MINUTES.between(startDateTime, endDateTime);
			//勤務時間を格納
			attendanceData.setMinutes(workMinutes);
			if (workMinutes > AttendanceData.REGULAR_WORK_TIME) {
				//残業時間があれば格納
				var duration = Duration.ofMinutes(workMinutes - AttendanceData.REGULAR_WORK_TIME);
				attendanceData.setOverTime(String.format("%02d:%02d", duration.toHours(), duration.toMinutes() % 60));
			}
		}

		//祝日もしくは「国民の休日」に該当すれば格納
		String holidayName = DateTimeUtil.getAllHolidaysMapByLocalDate(Year.of(workDay.getYear())).get(workDay);
		if (holidayName != null) {
			attendanceData.setHoliday(holidayName);
		}
		return attendanceData;
	}
}
