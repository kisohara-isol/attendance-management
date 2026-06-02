package com.example.attendance.util;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

public class LogUtil {
	private static final Logger logger = LoggerFactory.getLogger("ApplicationLogger");

	// SpringのMessageSourceを保持する静的変数
	private static MessageSource messageSource;

	// ★Spring起動時にMessageSourceを設定するためのメソッド
	public static void init(MessageSource source) {
		messageSource = source;
	}

	// =================================================================
	// 既存の汎用メソッド（エラーコードに関係ない自由なログ用）
	// =================================================================
	public static void info(String message, Object... args) {
		logger.info(message, args);
	}

	public static void warn(String message, Object... args) {
		logger.warn(message, args);
	}

	public static void debug(String message, Object... args) {
		logger.debug(message, args);
	}
	
	// =================================================================
	// エラーコード（E0001など）を指定してログを出すための専用メソッド
	// =================================================================
	public static void error(String errorCode, Object... args) {
		String message = errorCode;

		// messages.propertiesから文言の取得を試みる
		if (messageSource != null) {
			try {
				String rawMessage = messageSource.getMessage(errorCode, null, Locale.getDefault());
				// ログの見やすさのために「E0001: ログインIDが...」という形式にする
				message = errorCode + ": " + rawMessage;
			} catch (Exception e) {
				// 万が一プロパティファイルにコードが登録されていなかった場合のセーフティ
				message = errorCode + " (未定義のエラーコードです)";
			}
		}

		// 最終的なログ出力
		logger.error(message, args);
	}
}