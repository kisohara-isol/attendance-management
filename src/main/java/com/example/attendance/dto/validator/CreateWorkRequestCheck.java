package com.example.attendance.dto.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * CreateWorkRequestに付与すると、startTime、endTime、noteの値を検証し、<br>
 * <li>「出勤日」:startTime&lt;endTimeとなる数字が入力されている</li>
 * <li>「休み」:startTimeが「休み」でendTimeがnull</li>
 * <li>「有給」:「休み」でかつnoteが「有給」の二文字</li>
 * のいずれかに当てはまるか否かを検証する。
 * <p>「出勤日」のエラーメッセージはstartTimeに、<br>
 * 「休み」のエラーメッセージはendTimeに、<br>
 * 「有給」のエラーメッセージはnoteに付与される</p>
 * <p>出勤日については「出勤時間」「退勤時間」の数値はパースするため、<br>
 * バリデーションの順序付けで整数列以外をあらかじめはじく必要がある。</p>
 * 
 * @author kato
 * @version 2.0 2026-06-23 kato
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { CreateWorkRequestCheckValidator.class })
public @interface CreateWorkRequestCheck {

	/**
	 * 設定すると、このアノテーションが発する、全てのバリデーションメッセージを上書きする。
	 * アアノテーションに(message="内容")で設定できる。
	 * @return バリデーションメッセージ
	 */
	//Springの@Constraintアノテーションによって指定された抽象メソッド
	//抽象メソッドを設けることで、、アノテーション側に(message="")を書き込めるようになるほか、
	//バリデータ側でこのメッセージの値を参照できる
	String message() default ""; //{}でエラーコードを入力することで、messages.propatliesから値をとってきてくれる

	/**
	 * 「休み」の指定が不正であるバリデーションエラーの際に出るメッセージ。<br>
	 * アノテーションに(holidayMessage="内容")で上書きできる
	 * @return バリデーションメッセージ
	 */
	String holidayMessage() default "{W30005}";

	/**
	 * 「有給」の指定が不正であるバリデーションエラーの際に出るメッセージ。<br>
	 * アノテーションに(ptoMessage="内容")で上書きできる
	 * @return バリデーションメッセージ
	 */
	//独自に設定したパラメータ。扱いはmessage()と同じ
	String ptoMessage() default "{W30007}";

	/**
	 * 「退勤時間」が「出勤時間」より後でないバリデーションエラーの際に出るメッセージ。<br>
	 * アノテーションに(timeConmarisonMessage="内容")で上書きできる
	 * @return バリデーションメッセージ
	 */
	String timeComparisonMessage() default "{W30008}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}