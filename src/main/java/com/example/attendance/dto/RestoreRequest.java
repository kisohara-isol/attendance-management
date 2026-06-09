package com.example.attendance.dto;

import static java.util.Map.*;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Data;

/**
 * /attendance/management/restoreにおいて、画面から社員IDを受け取るDTO
 * @author kato
 */
@Data
public class RestoreRequest {

	/**
	 * 社員ID。nullでない正の整数
	 */
	@NotNull(message = "{W40001}")
	@Positive(message = "{W40002}")
	private Integer shainId;
	
	/**
	 * このdtoのフィールド(shainId)がもつアノテーションとエラーコードのマップ<br>
	 * <p>key=フィールドに付与されたアノテーション名<br>
	 * value=対応するエラーコード
	 */
	private static final Map<String, String> ANNOTATION_CODE = Map.ofEntries(
			entry("NotNull", "W40001"),
			entry("Positive", "W40002"));

	/**
	 * このdtoのshainIdフィールドがもつアノテーションの名前から、発生するエラーコードを取得する
	 * @param annotationName アノテーション名
	 * @return {}で囲ったエラーコード
	 */
	public static String getErrorCode(String annotationName) {
		return ANNOTATION_CODE.get(annotationName);
	}
}
