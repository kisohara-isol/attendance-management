package com.example.attendance.dto;

import static java.util.Map.*;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Data;

/**
 * 社員復旧画面において、ロック解除対象となる社員IDを受け取るためのデータ転送オブジェクト（DTO）。
 * <p>
 * Lombokの {@link Data} アノテーションにより、Getter/SetterやtoStringなどの共通メソッドが自動生成されます。
 * </p>
 * * @author kato
 */
@Data
public class RestoreRequest {

	/**
	 * 復旧対象の社員ID。
	 * <p>
	 * 必須入力項目（null不可）であり、かつ1以上の正の整数である必要があります。
	 * </p>
	 */
	@NotNull(message = "{W40001}")
	@Positive(message = "{W40002}")
	private Integer shainId;
	
	/**
	 * フィールドに付与されたバリデーションアノテーション名と、システム内部のエラーコードを紐付ける定数マップ。
	 * <p>
	 * キーにはアノテーションの簡易名（クラス名）、値には対応する管理用の警告コードを設定します。
	 * </p>
	 */
	private static final Map<String, String> ANNOTATION_CODE = Map.ofEntries(
			entry("NotNull", "W40001"),
			entry("Positive", "W40002"));

	/**
	 * デフォルトコンストラクタ。
	 */
	public RestoreRequest() {
	}

	/**
	 * 発生したバリデーションエラーの名前（コード）から、対応する内部エラーコードを安全に取得します。
	 * <p>
	 * 引数にプレフィックス（例: "NotNull.restoreRequest.shainId"）が含まれている場合でも、
	 * キーワード部分を部分一致で検知して適切なコードを逆引きします。該当がない場合はデフォルトの警告コードを返します。
	 * </p>
	 *
	 * @param annotationName Springの検証エラーから取得したエラーコードまたはアノテーション名
	 * @return 紐づく管理用エラーコード（"W40001" や "W40002"）。不一致時は一律で "W40000"
	 */
	public static String getErrorCode(String annotationName) {
		if (annotationName == null) {
			return "W40000";
		}
		
		// 💡【安全化】完全一致だけでなく、文字列に含まれているかで判定してNullPointerを完全防御！
		for (Map.Entry<String, String> entry : ANNOTATION_CODE.entrySet()) {
			if (annotationName.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		
		return "W40000"; // マップにない未知のエラーが起きた場合のセーフティコード
	}
}