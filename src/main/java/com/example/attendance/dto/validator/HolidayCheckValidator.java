package com.example.attendance.dto.validator;

import java.time.LocalTime;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.example.attendance.dto.CreateWorkRequest;

/**
 * \@HolidayCheckアノテーションの処理を記述したクラス
 * @author kato
 */
public class HolidayCheckValidator implements ConstraintValidator<HolidayCheck, CreateWorkRequest> {
	/**
	 * エラーメッセージ
	 */
	String message;

	/**
	 *エラーメッセージの初期化
	 */
	@Override
	public void initialize(HolidayCheck annotation) {
		//HolidayCheck側のメッセージを参照できるように
		this.message = annotation.message();
	}

	/**
	 * startTimeフィールドとendTimeフィールドがもつ値の組み合わせが<br>
	 * 「休み以外&有値」または「休み&空白」であるか検査する。
	 */
	@Override
	public boolean isValid(CreateWorkRequest value, ConstraintValidatorContext context) {
		//バリデーションの対象となるフィールドの値
		String start = value.getStartTime();
		LocalTime end = value.getEndTime();

		boolean isStartYasumi = "休み".equals(start);
		boolean isEndNull = end == null;

		//正常系1:startが「休み」じゃないかつendがnullじゃない(出勤日)
		if (!isStartYasumi && !isEndNull) {
			return true;
			//正常系2:startが「休み」かつendがnull(休日)
		} else if (isStartYasumi && isEndNull) {
			return true;
		}

		//エラーメッセージのセット
		//デフォルトメッセージ(DTO全体に付与される)の無効化
		context.disableDefaultConstraintViolation();

		context.buildConstraintViolationWithTemplate(message)//エラーメッセージはHolidayCheckから
				.addPropertyNode("endTime")//エラーメッセージが格納されるフィールドを設定
				.addConstraintViolation();

		return false;
	}

}
