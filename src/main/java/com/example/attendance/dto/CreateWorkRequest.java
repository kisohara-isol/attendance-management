package com.example.attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.attendance.dto.validator.HolidayCheck;

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
	
	/**「退勤時間(HHMM)」もしくはnull*/
	@DateTimeFormat(pattern = "HHmm")
	private LocalTime endTime;
	
	/**備考*/
	private String note;

	/**
	 * 引数無しのコンストラクタ
	 */
	public CreateWorkRequest() {
		// TODO 自動生成されたコンストラクター・スタブ
	}

}
