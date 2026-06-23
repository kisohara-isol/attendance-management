package com.example.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 勤務表のリストを取得するMapperクラス
 * @author Hagiwara
 */
@Mapper
public interface WorkTableMapper {
	
	/**
	 * 指定された社員および期間に応じた勤怠データを取得します。
	 * <p>
	 * 退勤時間（end_time）は、画面表示などの利便性を考慮し、
	 * 秒数を切り捨てた「HH:mm」形式の文字列（end_time_str）として取得されます。
	 * </p>
	 *
	 * @param shainId          取得対象の社員ID
	 * @param workMonthStart   対象期間の開始日（主に対象月の1日。この日付を含みます）
	 * @param workMonthEnd     対象期間の終了日（主に対象月の翌月1日。この日付は含みません）
	 * @return 勤怠データのリスト。1行分のデータは、カラム名をキー、値を値としたMapとして格納されます。
	 * （取得項目: shain_id, work_day, start_time, end_time_str, note）
	 */
	@Select("SELECT shain_id, work_day, start_time, "
			+ "SUBSTRING_INDEX(CAST(end_time AS CHAR), ':', 2) AS end_time_str, note "
			+ "FROM attendance_table "
			+ "WHERE shain_id = #{shainId} AND work_day >= #{workMonthStart} AND work_day < #{workMonthEnd} "
			+ "ORDER BY shain_id DESC")
	List<Map<String, Object>> selectAttendance(int shainId, LocalDate workMonthStart, LocalDate workMonthEnd);
	
}
