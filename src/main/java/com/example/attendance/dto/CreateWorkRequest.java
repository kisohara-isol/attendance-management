package com.example.attendance.dto;

import static java.util.Map.*;

import java.time.LocalDate;
import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import com.example.attendance.dto.validator.CreateWorkRequestCheck;
import com.example.attendance.dto.validator.ValidFirst;
import com.example.attendance.dto.validator.ValidSecond;
import com.example.attendance.dto.validator.ValidThird;

import lombok.Data;

/**
 * 勤務追加(workinput)画面の入力値。
 * @author kato
 * @version 2.0 2026-06-23 kato
 */
@Data
@CreateWorkRequestCheck(groups = ValidThird.class) //高度なバリデーションを実行する。
public class CreateWorkRequest {

	/**出勤日。yyyy/MM/dd*/
	//springbootのお節介で、余所での表記を流用してスラッシュ区切りにしてくれる…らしい
	private LocalDate workDay;

	/**「出勤時間(HHMM)」もしくは文字列「休み」*/
	@NotEmpty(message = "{W30002}", groups = ValidFirst.class)
	@Pattern(regexp = "^([0,1][0-9]|2[0-3])[0-5][0-9]|休み$", message = "{W30004}", groups = ValidSecond.class) //許容する値は「[0-23][0-59]」or「休み」。先にNotEmptyが実行される。
	private String startTime;

	/**
	 * 「退勤時間(HHMM)」もしくはnull<br>
	 * 24時間以上の値を許容する
	 */
	@Pattern(regexp = "^(([0-3][0-9]|4[0-7])[0-5][0-9])?$", message = "{W30006}", groups = ValidFirst.class) //許容する値は「[0-47][0-59]」or「空白」。
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
	 * このdtoの各フィールドがもつアノテーションとエラーコードの二次元マップ<br>
	 * <p>親mapKey=フィールド名<br>
	 * 子mapKey=フィールドに付与されたアノテーション名<br>
	 * 子mapValue=対応するエラーコード
	 */
	private static final Map<String, Map<String, String>> ANNOTATION_CODE = Map.ofEntries(
			entry("startTime", Map.ofEntries(
					entry("NotEmpty", "W30002"),
					entry("Pattern", "W30004"),
					entry("CreateWorkRequestCheck", "W30008"))),
			entry("endTime", Map.ofEntries(
					entry("typeMismatch", "W30006"),
					entry("CreateWorkRequestCheck", "W30005"))),
			entry("note", Map.ofEntries(
					entry("CreateWorkRequestCheck", "W30007"))));

	/**
	 * このdtoの各フィールドが持つアノテーションとエラーコードの二次元マップを返す
	 * @return Map
	 * <p>親mapKey=フィールド名<br>
	 * 子mapKey=フィールドに付与されたアノテーション名<br>
	 * 子mapValue=対応するエラーコード
	 */
	public static Map<String, Map<String, String>> getAnnotationCodeMap() {
		return ANNOTATION_CODE;
	}

}
