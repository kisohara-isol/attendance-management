package com.example.attendance.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.example.attendance.dto.CreateWorkRequest;

/**
 * \@CreateWorkRequestCheckアノテーションの処理を記述したクラス
 * @author kato
 * @version 2.0 2026-06-23 kato
 */
public class CreateWorkRequestCheckValidator implements ConstraintValidator<CreateWorkRequestCheck, CreateWorkRequest> {
	/**「休み」の指定が不正である旨を伝えるエラーメッセージ*/
	String holidayMessage;

	/**「有給」の指定が不正である旨を伝えるエラーメッセージ*/
	String ptoMessage;

	/**「出勤時間」と「退勤時間」の前後関係が不正である旨を伝えるエラーメッセージ*/
	String timeComparisonMessage;

	/**
	 *エラーメッセージの初期化
	 */
	@Override
	public void initialize(CreateWorkRequestCheck annotation) {
		String commonMessage = annotation.message();
		if (!commonMessage.isBlank()) {
			//共通メッセージが設定されている場合は全部を上書き
			this.holidayMessage = commonMessage;
			this.ptoMessage = commonMessage;
			this.timeComparisonMessage = commonMessage;

		} else {
			//HolidayCheck側のメッセージを参照できるように
			this.holidayMessage = annotation.holidayMessage();
			this.ptoMessage = annotation.ptoMessage();
			this.timeComparisonMessage = annotation.timeComparisonMessage();
		}
	}

	/**
	 * startTimeフィールドとendTimeフィールドがもつ値の組み合わせが<br>
	 * <li>「休み以外&有値」かつ「endTimeの数字のほうが大きい」</li>
	 * または
	 * <li>「休み&空白」</li>
	 * であるか、さらに
	 * <li>noteが「有給」ならstartTImeとendTimeは「休み&空白」</li>
	 * であるか検査する。
	 */
	@Override
	public boolean isValid(CreateWorkRequest value, ConstraintValidatorContext context) {

		//バリデーションエラー時のメッセージとフィールド
		String message = null;
		String field = null;

		//バリデーションの対象となるフィールドの値
		String start = value.getStartTime();
		String end = value.getEndTime();

		boolean isStartYasumi = "休み".equals(start);
		boolean isEndBlank = end.isBlank();
		boolean isNoteYukyu = value.getNote().equals("有給");

		if (isStartYasumi != isEndBlank) {
			//異常系:startとendの値の組み合わせ(「時間&時間」又は「休み&null」)が不正
			message = holidayMessage;
			field = "endTime";
		} else if (isNoteYukyu && !isStartYasumi) {
			//異常系:ノートが「有給」だがstartが「休み」じゃない
			message = ptoMessage;
			field = "note";
		} else if (!isStartYasumi) {
			if (!(Integer.parseInt(start) < Integer.parseInt(end))) {
				//異常系:endがstartより大きくない
				message = timeComparisonMessage;
				field = "startTime";
			}
		}

		if (message != null) {
			//バリデーションのデフォルトメッセージ(DTO全体に付与される)の無効化
			context.disableDefaultConstraintViolation();
			//異常系
			//エラーメッセージのセット
			context.buildConstraintViolationWithTemplate(message)
					.addPropertyNode(field)//エラーメッセージが格納されるフィールドを設定
					.addConstraintViolation();
			return false;
		}

		return true;
	}

}
