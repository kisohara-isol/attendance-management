package com.example.attendance.service;

import java.sql.Date;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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
			Time et = (Time) low.get("end_time");
			
			//曜日取得・格納
			DayOfWeek dw = workDay.toLocalDate().getDayOfWeek();
			String dayOfWeek = dw.getDisplayName(TextStyle.SHORT, Locale.JAPAN);
			ad.setDayOfWeek(dayOfWeek);
			
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd");
			String wd = workDay.toLocalDate().format(fmt);
			
			//出勤日・備考を格納
			ad.setWorkDay(wd);
			ad.setNote((String) low.get("note"));
			
			LocalTime startTime = st.toLocalTime();
			LocalTime endTime = et.toLocalTime();
			
			//勤務時間(分)を格納
			long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
			ad.setMinutes(minutes);

			//出勤時間・退勤時間が両方00:00の時は休み
			LocalTime breakTime = LocalTime.of(00, 00);
			if (startTime.equals(breakTime) && endTime.equals(breakTime)) {
				ad.setBreakDay(true);
			}
			ad.setStartTime(st.toLocalTime());
			ad.setEndTime(et.toLocalTime());
			
			//残業時間
			if (minutes > 480) {
				long over = minutes - 480;
				int overHour = (int) (over / 60);
				int overMinutes = (int) (over % 60);
				ad.setOverTime(String.format("%2d:%02d", overHour, overMinutes));
			}

			list.add(ad);
		}

		return list;
	}

}
