package com.example.attendance.dto;

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
}
