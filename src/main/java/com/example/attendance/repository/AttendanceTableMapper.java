package com.example.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.attendance.entity.AttendanceData;

/**
 * 勤務表のリストを取得するMapperクラス
 * @author Hagiwara
 * @version 2.0 2026-06-17 kato
 */
@Mapper
public interface WorkTableMapper {

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
}
