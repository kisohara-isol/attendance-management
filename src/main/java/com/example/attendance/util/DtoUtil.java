package com.example.attendance.util;

import java.util.Map;
import java.util.Optional;

/**
 * 各種dtoで汎用的に用いるユーティリティ
 */
public class DtoUtil {
	
	/**
	 * 第一引数のMapから、フィールド名、アノテーション名を指定してエラーコードを取得する<br>
	 * @param fieldErrors dtoの各フィールドがもつアノテーションとエラーコードの二次元マップ<br>
	 * <li>親mapKey:フィールド名</li>
	 * <li>子mapKey:フィールドに付与されたアノテーション名</li>
	 * <li>子mapValue:対応するエラーコード</li>
	 * @param field フィールド名
	 * @param annotationType アノテーション名
	 * @return 指定したフィールドの持つアノテーションが発するエラーコードを持つOptional<br>
	 * 指定した値がMap内に存在しない場合空のOptionalで帰ってくる
	 */
	public static Optional<String> getErrorCode(Map<String, Map<String, String>> fieldErrors, String field,
			String annotationType) {
		Map<String, String> errorsInaField = fieldErrors.get(field);
		if (errorsInaField == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(errorsInaField.get(annotationType));
	}
}
