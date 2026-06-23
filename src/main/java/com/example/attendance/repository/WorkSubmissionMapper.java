package com.example.attendance.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.example.attendance.entity.SalaryData;

/**
 * 給料データを取得するMapperインターフェース
 */
@Mapper
public interface WorkSubmissionMapper {
	/**
	 * 指定された社員IDに紐づく給与構成データを取得します。
	 * <p>
	 * {@code salary_components} テーブルから、該当する社員の基本給や
	 * 各種手当情報（割増倍率など）を1件取得し、{@link SalaryData} エンティティにマッピングします。
	 * </p>
	 *
	 * @param shainId 取得対象の社員ID
	 * @return 該当する社員の給与構成データ（{@link SalaryData}）。データが存在しない場合は {@code null}
	 */
	@Select("SELECT * FROM salary_components WHERE shain_id = #{shainId}")
	SalaryData selectSalaryData(@Param("shainId") int shainId);
}
