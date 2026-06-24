package com.example.attendance.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.attendance.entity.AttendanceData;

/**
 * 勤務表のリストを取得するMapperクラス
 * @author Hagiwara
 * @version 2.0 2026-06-17 kato
 */
@Mapper
public interface AttendanceTableMapper {

	/**
	 * 指定した社員ID、範囲の開始日、範囲終了の次の日を条件にattendance_tableを検索し、<br>
	 * 結果をAttendanceData型のListに格納します
	 * <p>
	 * 例えば、社員ID1の2020年4月(4/1-4/30)中の勤務データを取得したい場合、
	 * <blockquote>selectAttendancetoAttendanceData(1,LocalDate.of(2020,4,1),LocalDate,of(2020,5,1)</blockquote>
	 * と指定します。
	 * </p>
	 * @param shainId 社員ID
	 * @param workMonthStart 範囲の開始日
	 * @param workMonthEnd 範囲の終了日の次の日
	 * @return 該当する情報を保持したAttendanceDataのリスト
	 */
	@Select("SELECT * FROM attendance_table WHERE shain_id = #{shainId} AND work_day >= #{workMonthStart} "
			+ "AND work_day < #{workMonthEnd} ORDER BY shain_id DESC")
	List<AttendanceData> selectAttendancetoAttendanceData(
			int shainId, LocalDate workMonthStart, LocalDate workMonthEnd);

	/**
	 * 指定した社員ID、勤務日を条件にattendance_tableを検索し、<br>
	 * 結果をAttendanceData型のOptionalに格納します
	 * @param shainId 社員ID
	 * @param workDay 検索対象の勤務日
	 * @return 該当する情報を保持したAttendanceDataのOptional
	 */
	@Select("SELECT * FROM attendance_table WHERE shain_id = #{shainId} AND work_day = #{workDay}")
	Optional<AttendanceData> selectAttendanceByWorkDay(int shainId, LocalDate workDay);

	/**
	 * 勤怠データをテーブルに新規登録（インサート）します。
	 *
	 * @param shainId   社員ID
	 * @param workDay   出勤日
	 * @param startTime 出勤時間（未入力や休みの場合は 00:00）
	 * @param endTime   退勤時間（未入力の場合は 00:00）
	 * @param note      備考
	 */
	@Insert("INSERT INTO attendance_table (shain_id,work_day,start_time,end_time,note) VALUES (#{shainId},#{workDay},#{startTime},#{endTime},#{note})")
	void insertAttendanceData(
			@Param("shainId") int shainId,
			@Param("workDay") LocalDate workDay,
			@Param("startTime") LocalTime startTime,
			@Param("endTime") String endTime,
			@Param("note") String note);
	
	/**
	 *勤怠データを更新します。
	 *
	 * @param shainId   社員ID
	 * @param workDay   出勤日
	 * @param startTime 出勤時間（未入力や休みの場合は 00:00）
	 * @param endTime   退勤時間（未入力の場合は 00:00）
	 * @param note      備考
	 */
	@Update("UPDATE attendance_table SET start_time = #{startTime}, end_time = #{endTime}, note = #{note} WHERE shain_id = #{shainId} AND work_day = #{workDay} ")
	void updateAttendanceData(
			@Param("shainId") int shainId,
			@Param("workDay") LocalDate workDay,
			@Param("startTime") LocalTime startTime,
			@Param("endTime") String endTime,
			@Param("note") String note);
}
