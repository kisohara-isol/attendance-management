package com.example.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.example.attendance.dto.HolidayRule;
import com.example.attendance.entity.AttendanceData;

/**
 * 勤務表のリストを取得するMapperクラス
 */
@Mapper
public interface WorkTableMapper {

	/**
	 * 社員IDから月ごとの出勤票を抽出
	 * 💡 【最終解決】MySQLドライバによる「24時間丸め込みバグ」を完全に回避するため、
	 * 💡 CAST関数を使って、MySQLの内部で完全に『ただの文字列（CHAR型）』に変換してからJavaへ渡します。
	 */
	@Select("SELECT "
			+ "  shain_id AS shainId, "
			+ "  work_day AS workDay, "
			+ "  CAST(start_time AS CHAR) AS startTime, " // ⭕ TIME型からテキストに強制キャスト
			+ "  CAST(end_time AS CHAR) AS endTime, "     // ⭕ TIME型からテキストに強制キャスト
			+ "  note "
			+ "FROM attendance_table "
			+ "WHERE shain_id = #{shainId} "
			+ "  AND work_day >= #{workMonthStart} "
			+ "  AND work_day < #{workMonthEnd} "
			+ "ORDER BY work_day ASC")
	List<AttendanceData> selectAttendance(
			@Param("shainId") int shainId,
			@Param("workMonthStart") LocalDate workMonthStart,
			@Param("workMonthEnd") LocalDate workMonthEnd);

	/**
	 * 月から祝日を算出
	 */
	@Select("SELECT * FROM holiday_rules WHERE month = #{month}")
	List<HolidayRule> selectHoliday(int month);

	/**
	 * 指定された年月に、対象の社員のデータが何件登録されているかをカウントします。
	 */
	@Select("SELECT COUNT(*) FROM attendance_table " +
			"WHERE shain_id = #{shainId} " +
			"AND work_day BETWEEN " +
			"   STR_TO_DATE(CONCAT(#{year}, '-', LPAD(#{month}, 2, '0'), '-01'), '%Y-%m-%d') " +
			"AND LAST_DAY(STR_TO_DATE(CONCAT(#{year}, '-', LPAD(#{month}, 2, '0'), '-01'), '%Y-%m-%d'))")
	int countRegisteredDays(
			@Param("shainId") int shainId,
			@Param("year") String year,
			@Param("month") String month);
}