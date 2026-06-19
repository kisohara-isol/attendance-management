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
	 * このdtoの各フィールドがもつアノテーションとエラーコードの二次元マップ<br>
	 * <p>親mapKey=フィールド名<br>
	 * 子mapKey=フィールドに付与されたアノテーション名<br>
	 * 子mapValue=対応するエラーコード
	 */
	private static final Map<String, Map<String, String>> ANNOTATION_CODE = Map.ofEntries(
			entry("shainId", Map.ofEntries(
					entry("NotNull", "W40001"),
					entry("Positive", "W40002"))));

	/**
	 * このdtoの各フィールドが持つアノテーションとエラーコードの二次元マップを返す
	 * @return Map
	 * <p>親mapKey=フィールド名<br>
	 * 子mapKey=フィールドに付与されたアノテーション名<br>
	 * 子mapValue=対応するエラーコード
	 */
	public static Map<String, Map<String, String>> getAnnotationCodeMap() {
		return ANNOTATION_CODE;
	}
}
