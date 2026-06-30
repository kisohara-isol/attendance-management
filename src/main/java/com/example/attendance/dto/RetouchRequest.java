package com.example.attendance.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/**
 * 勤務修正画面からの入力値を保持するリクエストデータ転送オブジェクト（DTO）。
 *
 * @author Hagiwara
 */
@Data
public class RetouchRequest {
	
	/**
	 * 新しい出勤時間。
	 * <p>必須項目です。"休み" または "0900" のようなHHmm形式を期待します。</p>
	 */
	@NotBlank(message = "出勤時間が空白です。")
	private String startTime;
	
	/**
	 * 新しい退勤時間。
	 * <p>💡【修正】未入力によるパースクラッシュを防ぐため、こちらも必須バリデーションを追加します。</p>
	 */
	@NotBlank(message = "退勤時間が空白です。")
	private String endTime;
	
	/** 新しい備考（変更理由など） */
	private String note;

	/**
	 * デフォルトコンストラクタ。
	 */
	public RetouchRequest() {
	}
}