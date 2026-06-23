package com.example.attendance.dto;

import static java.util.Map.*;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/**
 * 勤務表照会画面からの検索リクエスト（フォーム入力値）を格納するDTO（データ転送オブジェクト）クラス。
 * <p>
 * 画面の入力項目（年・月）と1対1で対応しており、Controllerの引数で {@code @Validated} 
 * アノテーションを付与することで、Bean Validation（未入力チェックなど）が自動的に実行されます。
 * </p>
 *
 * @author Hagiwara
 */
@Data
public class WorkTableRequest {

	/** * 照会対象の年（必須入力）
	 * <p>
	 * 画面で未入力のまま送信された場合、「年を入力してください。」というエラーメッセージが生成されます。
	 * </p>
	 */
	@NotBlank(message = "年を入力してください。")
	private String workYear;

	/** * 照会対象の月（必須入力）
	 * <p>
	 * 画面で未入力のまま送信された場合、「月を入力してください。」というエラーメッセージが生成されます。
	 * </p>
	 */
	@NotBlank(message = "月を入力してください。")
	private String workMonth;

	/**
	 * このdtoの各フィールドがもつアノテーションとエラーコードの二次元マップ<br>
	 * <p>親mapKey=フィールド名<br>
	 * 子mapKey=フィールドに付与されたアノテーション名<br>
	 * 子mapValue=対応するエラーコード
	 */
	private static final Map<String, Map<String, String>> ANNOTATION_CODE = Map.ofEntries(
			entry("workYear", Map.ofEntries(entry("NotBlank", "W20001"))),
			entry("workMonth", Map.ofEntries(entry("NotBlank", "W20002"))));

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
