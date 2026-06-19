package com.example.attendance.dto;

import static java.util.Map.*;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

/**
 * @author Soeda
 * */

import lombok.Data;

@Data //getter,setterが自動敵に使えるようになる

public class LoginRequest {

	/** ログインID */
	@NotBlank(message = "{W10001}")
	private String loginId;

	/** パスワード */
	@NotBlank(message = "{W10002}")
	private String password;

	public LoginRequest() {

	}

	/**
	 * このdtoの各フィールドがもつアノテーションとエラーコードの二次元マップ<br>
	 * <p>親mapKey=フィールド名<br>
	 * 子mapKey=フィールドに付与されたアノテーション名<br>
	 * 子mapValue=対応するエラーコード
	 */
	private static final Map<String, Map<String, String>> ANNOTATION_CODE = Map.ofEntries(
			entry("loginId", Map.ofEntries(
					entry("NotBlank", "W10001"))),
			entry("password", Map.ofEntries(
					entry("NotBlank", "W10002"))));

	
	/**
	 * このdtoの各フィールドが持つアノテーションとエラーコードの二次元マップを返す
	 * @return Map
	 * <p>親mapKey=フィールド名<br>
	 * 子mapKey=フィールドに付与されたアノテーション名<br>
	 * 子mapValue=対応するエラーコード
	 */
	public static Map<String,Map<String,String>> getAnnotationCodeMap(){
		return ANNOTATION_CODE;
	}
}
