package com.example.attendance.dto.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * クラスレベルに付与し、出退勤時間の「休み入力」および「前後関係」を複合的に検証するカスタムバリデーションアノテーション。
 * <p>
 * 主に以下の相関チェックを制御します：
 * <ul>
 * <li>出勤時間が「休み」の際、退勤時間が空欄であることを要請。</li>
 * <li>通常の出勤時、退勤時間が入力されており、かつ出勤時刻より未来であることを要請。</li>
 * </ul>
 * </p>
 *
 * @author kato
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { HolidayCheckValidator.class })
public @interface HolidayCheck {

	/**
	 * バリデーションエラー発生時に返却されるデフォルトのエラーメッセージ（またはメッセージプロパティキー）。
	 *
	 * @return エラーメッセージのテンプレート（デフォルトは "{W30005}"）
	 */
	String message() default "{W30005}";

	/**
	 * バリデーションのグループ化（評価順序の制御）に利用する属性。
	 *
	 * @return グループクラスの配列
	 */
	Class<?>[] groups() default {};

	/**
	 * チェックエラー時に、特定の重要度やカスタムメタデータ（ペイロード）を付与するための属性。
	 *
	 * @return ペイロードクラスの配列
	 */
	Class<? extends Payload>[] payload() default {};
}