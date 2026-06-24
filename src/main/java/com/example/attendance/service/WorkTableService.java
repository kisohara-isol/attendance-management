package com.example.attendance.service;

import java.util.List;

import com.example.attendance.entity.AttendanceData;

/**
 * 勤務表に関する業務ロジックを提供するサービス実装クラスです。
 * <p>
 * データベースから取得した生の勤怠データを基に、画面表示や集計に適した形式
 * （曜日、フォーマット済み日付、実労働時間、残業時間など）への加工・計算を行います。
 * </p>
 *
 * @author Hagiwara
 */
public interface WorkTableService {

	/**
	 * 指定された社員および年月に紐づく勤怠明細一覧を取得・加工します。
	 * <p>
	 * データベースから取得した生の勤怠データを、曜日、フォーマット済みの年月日、
	 * 実労働時間、残業時間などを計算・設定したオブジェクトのリストに変換します。
	 * </p>
	 *
	 * @param shainId 対象の社員ID
	 * @param year    対象の年 (例: 2026)
	 * @param month   対象の月 (1〜12)
	 * @return 加工済みの勤怠明細データ {@link AttendanceData} のリスト
	 * @throws java.time.DateTimeException 年月の指定が不正な場合に発生する可能性があります
	 */
	List<AttendanceData> getAttendanceList(int shainId, int year, int month);

	List<AttendanceData> createAttendanceDatasFromDB(int shainId, int year, int month);
}
