package com.example.attendance.repository;

import java.time.LocalDate;
import java.time.LocalTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 勤務修正処理に関するデータベースアクセスを制御するマッパーインターフェース。
 *
 * @author Hagiwara
 */
@Mapper
public interface RetouchMapper {
	
	/**
	 * 出退勤テーブルのレコードを更新します。
	 * <p>
	 * 💡【修正】WHERE句の `note` 条件に `COALESCE` 関数を適用しました。
	 * これにより、DB上の既存レコードの備考が `NULL` であっても、空文字として安全にマッチングが行われ、
	 * 確実にUPDATE文が成功するようになります。
	 * </p>
	 *
	 * @param newStart  新しい出勤時間 (HH:mm)
	 * @param newEnd    新しい退勤時間 (HH:mm)
	 * @param newNote   新しい備考（変更理由など）
	 * @param shainId   対象の社員ID
	 * @param workday   対象の日付 (YYYY-MM-DD)
	 * @param start     変更前の出勤時間（レコード特定用）
	 * @param end       変更前の退勤時間（レコード特定用）
	 * @param memo      変更前の備考（レコード特定用）
	 */
	@Update("UPDATE attendance_table SET start_time = #{newStart}, end_time = #{newEnd}, note = #{newNote} "
			+ "WHERE shain_id = #{shainId} AND work_day = #{workday} AND start_time = #{start} AND "
			+ "end_time = #{end} AND COALESCE(note, '') = #{memo}")
	void updateAttendanceTable(
		@Param("newStart") LocalTime newStart, 
		@Param("newEnd") LocalTime newEnd, 
		@Param("newNote") String newNote, 
		@Param("shainId") int shainId, 
		@Param("workday") LocalDate workday, 
		@Param("start") LocalTime start, 
		@Param("end") LocalTime end, 
		@Param("memo") String memo
	);
}