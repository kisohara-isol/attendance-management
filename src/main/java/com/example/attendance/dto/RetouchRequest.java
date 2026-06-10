package com.example.attendance.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;
/**
 * 勤務修正画面からの入力値を保持するリクエストデータ転送オブジェクト（DTO）。
 * <p>
 * ユーザーが画面上で入力した「新しい出勤時間」「新しい退勤時間」「新しい備考」の値を
 * コントローラーへ一括して引き渡すために使用されます。
 * 出勤時間に対しては、未入力（空文字）を防ぐためのバリデーションチェックが設定されています。
 * </p>
 * * @author Hagiwara
 */
@Data
public class RetouchRequest {
	
	/**出勤時間*/
	@NotBlank(message = "出勤時間が空白です。")
	private String startTime;
	/**退勤時間*/
	private String endTime;
	/**備考*/
	private String note;
}
