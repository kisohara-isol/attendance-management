package com.example.attendance.dto;

import java.time.LocalDate;

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

	/** 出勤日。yyyyMMdd */
	@DateTimeFormat(pattern = "yyyyMMdd")
	@NotNull(message = "{W30001}")
	private LocalDate workDay;

	/** 「出勤時間(HHMM)」もしくは文字列「休み」 */
	@NotEmpty(message = "{W30002}")
	@Pattern(regexp = "^(([0-3][0-9]|4[0-7])[0-5][0-9]|休み)?$", message = "{W30004}") // 💡出勤側も24時超え入力や「休み」を考慮
	private String startTime;

	/** 「退勤時間(HHMM)」もしくはnull */
	// 💡 @NotEmpty を削除しました。チェックは @HolidayCheck 内で出し分けます。
	@Pattern(regexp = "^$|^([0-3][0-9]|4[0-7])[0-5][0-9]$", message = "{W30004}") // 💡空文字("^$")も正規表現で許容
	private String endTime;

	/** 備考 */
	private String note;

	/**
	 * 引数無しのコンストラクタ
	 */
	public CreateWorkRequest() {
	}
}