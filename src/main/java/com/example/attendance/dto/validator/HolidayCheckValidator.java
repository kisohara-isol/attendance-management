package com.example.attendance.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.example.attendance.dto.CreateWorkRequest;

public class HolidayCheckValidator implements ConstraintValidator<HolidayCheck, CreateWorkRequest> {

	@Override
	public boolean isValid(CreateWorkRequest request, ConstraintValidatorContext context) {
		if (request == null) {
			return true;
		}

		String startTime = request.getStartTime();
		String endTime = request.getEndTime();

		// 1. 出勤時間が未入力（空文字またはnull）の場合は単項目チェック（@NotNullなど）に任せる
		if (startTime == null || startTime.isEmpty()) {
			return true;
		}

		// 💡 2. 出勤時間が「休み」の場合
		if ("休み".equals(startTime)) {
			// 退勤時間が入力されてしまっていたらエラー
			if (endTime != null && !endTime.isEmpty()) {
				context.disableDefaultConstraintViolation();
				// アノテーション標準のエラーメッセージ（{W30005}：休みの場合は退勤時間を空欄に…等）を出す
				context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
						.addPropertyNode("endTime").addConstraintViolation();
				return false;
			}
			return true; // ➔ 出勤「休み」かつ退勤「空欄」なので、100%正常として通過！
		}

		// 3. 出勤時間が「休み」以外（通常の時間）の場合、退勤時間は【必須入力】にする
		if (endTime == null || endTime.isEmpty()) {
			context.disableDefaultConstraintViolation();
			// 💡 messages.properties に定義されている退勤必須エラーコード「{W30003}」を呼び出します
			context.buildConstraintViolationWithTemplate("{W30003}")
					.addPropertyNode("endTime").addConstraintViolation();
			return false;
		}

		// 4. 出勤時間と退勤時間の前後関係チェック（出勤 ＞ 退勤 になっていないか）
		try {
			int start = Integer.parseInt(startTime);
			int end = Integer.parseInt(endTime);
			if (start >= end) {
				context.disableDefaultConstraintViolation();
				// 💡 時間前後エラーコード「{W30004}」（または適切なエラーコード）を呼び出します
				context.buildConstraintViolationWithTemplate("{W30004}")
						.addPropertyNode("endTime").addConstraintViolation();
				return false;
			}
		} catch (NumberFormatException e) {
			// 形式エラーは @Pattern 側が検知するので、ここではスルー
		}

		return true;
	}
}