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
	
	@Select("SELECT * FROM attendance_table WHERE shain_id = #{shainId} AND work_day >= #{workMonthStart} "
			+ "AND work_day < #{workMonthEnd} ORDER BY shain_id DESC")
	List<Map<String, Object>> selectAttendance(int shainId, LocalDate workMonthStart, LocalDate workMonthEnd);
	
}
