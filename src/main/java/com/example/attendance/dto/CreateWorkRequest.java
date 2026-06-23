package com.example.attendance.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.attendance.dto.validator.HolidayCheck;
import com.example.attendance.util.CreateWorkRequestFields;

import lombok.Data;

/**
 * 勤務追加(workinput)画面の入力値。
 * @author kato
 */
@Data
@HolidayCheck
public class CreateWorkRequest {

	/**出勤日。yyyyMMdd*/
	@DateTimeFormat(pattern = "yyyyMMdd")
	//NotEmptyはLocalDateに使えない。厳密にはNotNullだと判定対象が違うらしいが空文字のチェックはできている
	@NotNull(message = "{W30001}") //messages.propertiesにあるW30001の値を取得
	private LocalDate workDay;
	
	/**「出勤時間(HHMM)」もしくは文字列「休み」*/
	@NotEmpty(message = "{W30002}")
	@Pattern(regexp="^(([0,1][0-9]|2[0-3])[0-5][0-9]|休み)?$", message = "{W30004}") //許容する値は「[0-24][0-59]」or「休み」or「空白」。三つめはNotEmptyのほうに引っかかる
	private String startTime;
	
	/**
	 * 「退勤時間(HHMM)」もしくはnull<br>
	 * 24時間以上の値を許容する
	 */
	private String endTime;
	
	/**備考*/
	private String note;

	/**
	 * 引数無しのコンストラクタ
	 */
	public CreateWorkRequest() {
		// TODO 自動生成されたコンストラクター・スタブ
	}
	
	/**
	 * 渡されたフィールド名とアノテーション名に対応するエラーコードを取得する
	 * @param field フィールド名
	 * @param annotationType アノテーション名
	 * @return エラーコード
	 */
	public static String getErrorCode(String field, String annotationType) {
		//フィールド名に対応する列挙子を取得
		CreateWorkRequestFields fieldEnum = switch (field) {
		case "workDay" -> CreateWorkRequestFields.WORK_DAY;
		case "startTime" -> CreateWorkRequestFields.START_TIME;
		case "endTime" -> CreateWorkRequestFields.END_TIME;
		case "note" -> CreateWorkRequestFields.NOTE;
		default -> throw new IllegalArgumentException("Unexpected value: " + field);
		};
		return fieldEnum.getErrorCode(annotationType);
	}

}
