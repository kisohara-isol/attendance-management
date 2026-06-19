package com.example.attendance.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;

import org.springframework.validation.BindingResult;

/**
 * Controllerで汎用的に用いるUtilityクラス
 */
public class ControllerUtil {
	/**
	 * 引数に渡したHttpSessionの中に、指定の名前で保存されたセッションが存在するかを調べる
	 * @param session 中身を調べたいセッション
	 * @param name 対象の名前を指定
	 * @return nameに指定されたセッションが存在すればtrue
	 */
	public static boolean isKeepingSession(HttpSession session, String name) {
		return Collections.list(session.getAttributeNames()) //sessionに保存されている中身の名前をlistに
				.stream().anyMatch(x -> name.equals(x));
	}

	/**
	 * 引数に渡したBindingResultが持つ全てのバインドエラーをログ出力する
	 * @param result バインドエラーを持つBindingResult
	 * @param getErrorCode エラーコードを取得するための関数<br>
	 * 						第一引数はエラーを発したフィールド名、第二引数はアノテーション名
	 */
	public static void warnAllBindErrors(BindingResult result, BinaryOperator<String> getErrorCode) {
		result.getFieldErrors().forEach(
				x -> {
					String field = x.getField();
					String annotationType = x.getCode();
					LogUtil.warn(
							//渡されたエラーコード取得関数を適用
							getErrorCode.apply(field, annotationType));
				});
	}

	/**
	 * 引数に渡したBindingResultが持つ全てのバインドエラーをログ出力する
	 * @param result バインドエラーを持つBindingResult
	 * @param errorCodes エラーコードを取得するためのマップ
	 * 
	 */
	public static void warnAllBindErrors(BindingResult result, Map<String, Map<String, String>> errorCodes) {
		ControllerUtil.collectAllBindErrorCodes(result, errorCodes).forEach(x -> LogUtil.warn(x));
	}

	/**
	 * 引数に渡したBindingResultが持つ全てのバインドエラーのエラーコードを取得する
	 * @param result バインドエラーを持つBindingResult
	 * @param errorCodes エラーコードを取得するためのマップ
	 * @return エラーコードのList
	 * 
	 */
	public static List<String> collectAllBindErrorCodes(BindingResult result,
			Map<String, Map<String, String>> errorCodes) {
		List<String> errorCodeList = result.getFieldErrors().stream()
				.map(
						x -> {
							String field = x.getField();
							String annotationType = x.getCode();
							Optional<String> wrapedErrorCode = DtoUtil.getErrorCode(errorCodes, field, annotationType);
							if (wrapedErrorCode.isEmpty()) {
								LogUtil.error("定義されていないフィールド・アノテーションのバインドエラー:field={},annotationType={}", field,
										annotationType);
							}
							return wrapedErrorCode.orElse(null);
						})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		return errorCodeList;
	}

}
