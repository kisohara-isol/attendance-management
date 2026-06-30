package com.example.attendance.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/**
 * ログイン画面からのリクエストパラメータ（入力値）を保持するデータ転送オブジェクト（DTO）。
 * <p>
 * Lombokの {@link Data} アノテーションにより、各フィールドのGetter/Setter、
 * toString、equals、hashCode などのメソッドがコンパイル時に自動生成されます。
 * </p>
 *
 * @author Soeda
 */
@Data
public class LoginRequest {

	/** * ログインID
	 * <p>
	 * 必須入力項目です。空文字（空白スペースのみを含む）はバリデーションエラーとなります。
	 * </p>
	 */
	@NotBlank(message = "ログインIDを入力してください")
	private String loginId;

	/** * パスワード
	 * <p>
	 * 必須入力項目です。空文字（空白スペースのみを含む）はバリデーションエラーとなります。
	 * </p>
	 */
	@NotBlank(message = "パスワードを入力してください")
	private String password;

	/**
	 * デフォルトコンストラクタ。
	 * <p>
	 * Spring MVCでのフォームデータの自動バインド（インスタンス化）のために必要となります。
	 * </p>
	 */
	public LoginRequest() {
	}
}