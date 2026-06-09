package com.example.attendance.repository;

import java.time.LocalDate;
import java.time.LocalTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
/**
 * 勤務修正処理に関するデータベースアクセスを制御するマッパーインターフェース。
 * <p>
 * MyBatis（@Updateアノテーション）を使用し、attendance_table に対する
 * 勤務実績データの更新（UPDATE文の実行）を担当します。
 * </p>
 *
 * @author Hagiwara
 */
@Mapper
public interface RetouchMapper {
	
	/**
	 * 出退勤テーブルのレコードを更新します。
	 * <p>
	 * 指定された社員ID、日付、および変更前の出退勤時間・備考に一致するレコードを特定し、
	 * 新しい出勤時間、退勤時間、備考で上書きします。
	 * </p>
	 *
	 * @param newStart   新しい出勤時間 (HH:mm)
	 * @param newEnd     新しい退勤時間 (HH:mm)
	 * @param newNote    新しい備考（変更理由など）
	 * @param shainId    対象の社員ID
	 * @param workday    対象の日付 (YYYY-MM-DD)
	 * @param start      変更前の出勤時間（レコード特定用）
	 * @param end        変更前の退勤時間（レコード特定用）
	 * @param memo       変更前の備考（レコード特定用）
	 */
	@Update("UPDATE attendance_table SET start_time = #{newStart}, end_time = #{newEnd}, note = #{newNote} " // 末尾にスペース追加
	        + "WHERE shain_id = #{shainId} AND work_day = #{workday} AND start_time = #{start} AND "         // 末尾にスペース追加
	        + "end_time = #{end} AND note = #{memo}")
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
