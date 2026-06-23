package com.example.attendance.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.attendance.entity.AttendanceData;
import com.example.attendance.repository.WorkTableMapper;
import com.example.attendance.util.DateTimeUtil;
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

	/**
	 * 指定された社員および年月に紐づく勤怠明細一覧を取得・加工します。
	 * <p>
	 * データベースから取得した生の勤怠データを、曜日、フォーマット済みの年月日、 実労働時間、残業時間などを計算・設定したオブジェクトのリストに変換します。
	 * </p>
	 *
	 *@author Hagiwara
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

		//勤務実績データの取得
		List<AttendanceData> allAttendanceDatas = createAttendanceDatasFromDB(shainId, start, end);

		//この年の全祝日のマップを取得
		Map<LocalDate, String> holidays = DateTimeUtil.getAllHolidaysMapByLocalDate(Year.of(year));

		//最終的に返却する全日分のリスト
		List<AttendanceData> resultList = new ArrayList<>();

		//一日分ずつ、勤務実績のリストを修正
		for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
			String dateString = date.format(DateTimeUtil.SLASH_DATE_FORMAT);

			var oneDayAttendance = allAttendanceDatas.stream()
					.filter(x -> x.getWorkDay().equals(dateString))
					.findFirst().orElse(null);
			if (oneDayAttendance == null) {
				//対象月において、勤務実績のない日には空データを挿入する形で全ての日を埋める
				oneDayAttendance = new AttendanceData();
				oneDayAttendance.setShainId(shainId);
				oneDayAttendance.setWorkDay(dateString);
				oneDayAttendance.setDayOfWeek(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPAN));
				if (holidays.containsKey(date)) {
					//祝日の場合はその名前を入れる
					oneDayAttendance.setHoliday(holidays.get(date));
				}
			}

			resultList.add(oneDayAttendance);
		}

		return resultList;
	}

	/**
	 * 指定された社員idと日付の範囲で該当する勤務実績データを一覧で取得したのち、<br>
	 * 書式の変更や付随情報の設定などのセットアップを行う。
	 * @param shainId 社員ID
	 * @param startDate 検索対象となる日付の始点
	 * @param nextOfEndDate 検索対象となる日付の終点の翌日
	 * @return セットアップされた勤務実績データのリスト
	 */
	private List<AttendanceData> createAttendanceDatasFromDB(int shainId, LocalDate startDate,
			LocalDate nextOfEndDate) {
		//DBアクセス
		List<AttendanceData> allDBEntries = workTableMapper
				.selectAttendancetoAttendanceData(shainId, startDate, nextOfEndDate);
		LogUtil.info("データベース[attendance_table]にアクセスしました。");
		//AttendanceDataのセットアップ
		allDBEntries.forEach(x -> setUpAttendanceData(x));
		return allDBEntries;
	}

	/**
	 * 受け取った勤務実績データに対して、日付と時間の書式変更や<br>
	 * 休日判定、勤務時間と残業時間の設定などのセットアップを行う。
	 * @param attendanceData 対象の勤務実績データ
	 * @return セットアップされた勤務実績データ
	 */
	private static AttendanceData setUpAttendanceData(AttendanceData attendanceData) {
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
