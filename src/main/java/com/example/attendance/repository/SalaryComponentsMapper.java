package com.example.attendance.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.attendance.entity.SalaryComponentsData;

/**
 * salary_componentsのデータを取得するMapper
 */
@Mapper
public interface SalaryComponentsMapper {
	/**
	 * 社員IDを指定することで、紐づいた給与構成のエントリを返します
	 * @param shainId 検索対象の社員ID
	 * @return Optional型で、社員IDがマッチすればSalaryComponentsDataが格納される。
	 */
	@Select("SELECT * FROM salary_components WHERE shain_id = #{shainId}")
	Optional<SalaryComponentsData> selectSalaryComponentsByShainID(int shainId);
		
}
