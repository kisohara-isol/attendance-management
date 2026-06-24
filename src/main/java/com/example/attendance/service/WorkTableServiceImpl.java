package com.example.attendance.service;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.attendance.entity.AttendanceData;
import com.example.attendance.repository.AttendanceTableMapper;
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
	AttendanceTableMapper workTableMapper;

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
		List<AttendanceData> allAttendanceDatas = createAttendanceDatasFromDB(shainId, year, month);

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
	 * 指定された社員idと年月で該当する勤務実績データを一覧で取得したのち、<br>
	 * 書式の変更や付随情報の設定などのセットアップを行う。
	 * @param shainId 社員ID
	 * @param year 検索対象となる年
	 * @param month 検索対象となる月
	 * @return セットアップされた勤務実績データのリスト
	 */
	@Override
	public List<AttendanceData> createAttendanceDatasFromDB(int shainId, int year,
			int month) {

		LocalDate startDate = LocalDate.of(year, month, 1);
		LocalDate nextOfEndDate = LocalDate.of(year, month, 1).plusMonths(1);
		//DBアクセス
		List<AttendanceData> allDBEntries = workTableMapper
				.selectAttendancetoAttendanceData(shainId, startDate, nextOfEndDate);
		LogUtil.info("データベース[attendance_table]にアクセスしました。");
		//AttendanceDataのセットアップ
		allDBEntries.forEach(x -> AttendanceData.setUpAttendanceData(x));
		return allDBEntries;
	}

}
