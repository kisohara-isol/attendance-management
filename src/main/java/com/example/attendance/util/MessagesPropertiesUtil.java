package com.example.attendance.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * messages.propertiesの値を参照するためのユーティリティクラス
 */
public class MessagesPropertiesUtil {
	private static final ResourceBundle rb = ResourceBundle.getBundle("messages");

	/**
	 * messages.propertiesからkeyに紐づいた値を文字列で取得する
	 * @param key キー
	 * @return 対応する値
	 */
	public static String getErrorMessage(String key) {
		try {
			return rb.getString(key);
		} catch (MissingResourceException e) {
			// キーが存在しない場合のハンドリング
			return null;
		}
	}
}
