package com.example.attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;
/**
 * 勤務修正に関するビジネスロジックを統括するサービスインターフェース。
 * <p>
 * コントローラーからの修正リクエストを受け取り、バリデーション通過後のデータを
 * リポジトリ層（Mapper）へと引き渡すための仲介（ビジネスルール定義）を行います。
 * </p>
 *
 * @author Hagiwara
 */
public interface RetouchService {
	/**
	 * 出退勤テーブルのレコードを更新します。
	 * <p>
	 * 指定された社員ID、日付、および変更前の出退勤時間・備考に一致するレコードを特定し、 新しい出勤時間、退勤時間、備考で上書きします。
	 * </p>
	 *
	 * @param newStart 新しい出勤時間 (HH:mm)
	 * @param newEnd   新しい退勤時間 (HH:mm)
	 * @param newNote  新しい備考（変更理由など）
	 * @param shainId  対象の社員ID
	 * @param workday  対象の日付 (YYYY-MM-DD)
	 * @param start    変更前の出勤時間（レコード特定用）
	 * @param end      変更前の退勤時間（レコード特定用）
	 * @param memo     変更前の備考（レコード特定用）
	 */
	void retouchAttendance(LocalTime newStart, LocalTime newEnd, String newNote, int shainId, LocalDate workday,
	LocalTime start, LocalTime end, String memo);
}
