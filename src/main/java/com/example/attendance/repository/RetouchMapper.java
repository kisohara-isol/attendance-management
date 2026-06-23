package com.example.attendance.repository;

import java.time.LocalDate;
import java.time.LocalTime;

import org.apache.ibatis.annotations.Insert;
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
	@Update("UPDATE attendance_table SET start_time = #{newStart}, end_time = #{newEnd}, note = #{newNote} "
	        + "WHERE shain_id = #{shainId} AND work_day = #{workday}")
	void updateAttendanceTable(
	    @Param("newStart") LocalTime newStart, 
	    @Param("newEnd") String newEnd, 
	    @Param("newNote") String newNote, 
	    @Param("shainId") int shainId, 
	    @Param("workday") LocalDate workday, 
	    @Param("start") LocalTime start, 
	    @Param("end") String end, 
	    @Param("memo") String memo
	);

	/**
	 * 勤務表テーブル（attendance_table）に新しい勤務レコードを1件登録します。 *
	 * <p>
	 * 社員ID、勤務日、始業・終業時間、および備考をデータベースに永続化します。
	 * </p>
	 * * @param shainId 登録対象の社員ID
	 * 
	 * @param workDay   勤務対象の日付 ({@link LocalDate})
	 * @param startTime 始業時刻 ({@link LocalTime})
	 * @param endTime   終業時刻（※文字列形式、未入力や退勤前などの状態を考慮した型）
	 * @param note      勤務に関する備考・特記事項（連絡事項や遅刻理由など）
	 */
	@Insert("INSERT INTO attendance_table (shain_id, work_day, start_time, end_time, note) VALUES (#{shainId}, #{workDay}, #{startTime}, #{endTime}, #{note})")
	void insertAttendanceTable(@Param("shainId") int shainId, @Param("workDay") LocalDate workDay,
			@Param("startTime") LocalTime startTime, @Param("endTime") String endTime, @Param("note") String note);
}
