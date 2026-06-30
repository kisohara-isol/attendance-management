package com.example.attendance.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.example.attendance.dto.CreateWorkRequest;

/**
 * {@link HolidayCheck} アノテーションの実体として、勤務登録時の相関入力チェックを行う検証バリデータクラス。
 * <p>
 * 画面から送られた出退勤の文字列を解析し、「休み時の退勤欄の制限」「出勤時の退勤必須判定」「時間の前後関係チェック」を網羅します。
 * </p>
 */
public class HolidayCheckValidator implements ConstraintValidator<HolidayCheck, CreateWorkRequest> {

	/**
	 * フォームから送信された勤務データがビジネスルール（相関制約）を満たしているか検証します。
	 *
	 * @param request  検証対象の入力フォームDTO (CreateWorkRequest)
	 * @param context  エラーメッセージの上書きやエラー発生ノードの追加を行うコンテキストオブジェクト
	 * @return 検証を通過した（エラーがない）場合はtrue、ルール違反を検知した場合はfalse
	 */
	@Override
	public boolean isValid(CreateWorkRequest request, ConstraintValidatorContext context) {
		if (request == null) {
			return true;
		}

		String startTime = request.getStartTime();
		String endTime = request.getEndTime();

		// 1. 出勤時間が未入力の場合は、単項目チェック（@NotBlankなど）に検証を委ねる
		if (startTime == null || startTime.isEmpty()) {
			return true;
		}

		// 2. 出勤時間が「休み」の場合
		if ("休み".equals(startTime)) {
			// 退勤時間が入力されてしまっていたらエラー
			if (endTime != null && !endTime.isEmpty()) {
				context.disableDefaultConstraintViolation();
				// アノテーションで定義されている標準メッセージ（{W30005}）を、退勤時間（endTime）の項目に紐づけて発火
				context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
						.addPropertyNode("endTime").addConstraintViolation();
				return false;
			}
			return true; // 出勤「休み」かつ退勤「空欄」なので、正常として通過
		}

		// 3. 出勤時間が「休み」以外（通常の時間）の場合、退勤時間は【必須入力】
		if (endTime == null || endTime.isEmpty()) {
			context.disableDefaultConstraintViolation();
			// messages.properties に定義されている退勤必須エラーコード「{W30003}」を退勤欄にマッピング
			context.buildConstraintViolationWithTemplate("{W30003}")
					.addPropertyNode("endTime").addConstraintViolation();
			return false;
		}

		// 4. 出勤時間と退勤時間の前後関係チェック（出勤 ＞ 退勤 になっていないか）
		try {
			int start = Integer.parseInt(startTime.replaceAll("[^0-9]", ""));
			int end = Integer.parseInt(endTime.replaceAll("[^0-9]", ""));
			if (start >= end) {
				context.disableDefaultConstraintViolation();
				// 時間前後エラーコード「{W30004}」を退勤欄にマッピングしてエラーにする
				context.buildConstraintViolationWithTemplate("{W30004}")
						.addPropertyNode("endTime").addConstraintViolation();
				return false;
			}
		} catch (NumberFormatException e) {
			// 数値へのパースエラー（形式異常）は単項目チェック（@Patternなど）が検知するため、ここではスルー
		}

		return true;
	}
}