package com.example.attendance.dto.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * クラス自体に付与すると、endTimeの値がnullの時に、startTimeに「休み」が入力されていることを要請する。
 * @author kato
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { HolidayCheckValidator.class })
public @interface HolidayCheck {

	/**
	 * バリデーションエラーの際に出るメッセージ。<br>
	 * アノテーションに(message="内容")で上書きできる
	 * @return バリデーションメッセージ
	 */
	//Springの@Constraintアノテーションによって指定された抽象メソッド
	//抽象メソッドを設けることで、、アノテーション側に(message="")を書き込めるようになるほか、
	//バリデータ側でこのメッセージの値を参照できる
	String message() default "{W30005}"; //{}でエラーコードを入力することで、messages.propatliesから値をとってきてくれる

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}