package com.example.attendance.util;

import static java.util.Map.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * dtoクラス"CreateWorkRequest"に存在するフィールドの列挙子。<br>
 * 各列挙子は、自身の表すフィールドが持つアノテーションを、{アノテーション名,エラーコード}のMapで保持する
 */
public enum CreateWorkRequestFields {
	/**出勤日(workDay)*/
	// ★変更：Map.entryを java.util.List.of() で囲むように修正
	WORK_DAY(List.of(entry("typeMismatch", "W30003"), entry("NotNull", "W30001"))),
	/**出勤時間(startTime)*/
	START_TIME(List.of(entry("NotEmpty", "W30002"), entry("Pattern", "W30004"))),
	/**退勤時間(endTIme)*/
	END_TIME(List.of(entry("typeMismatch", "W30006"), entry("HolidayCheck", "W30005"))),
	/**備考(note)*/
	// 空のリストを渡す
	NOTE(List.of());

	/**列挙子が持つ、{アノテーション名,エラーコード}のmap*/
	private Map<String, String> annotations;

	/**コンストラクタ
	 * @param entriesList 各フィールドのアノテーションとエラーコードのリスト
	 */
	// ★変更：可変引数（...）をやめて、Listで受け取るように修正
	private CreateWorkRequestFields(List<Map.Entry<String, String>> entriesList) {
		// Stream APIを使って、Listの中身をMapに変換して詰め込む
		this.annotations = entriesList.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * 引数で指定されたアノテーション名に対応するエラーコードを返す。
	 * @param annotationType アノテーション名
	 * @return エラーコード。対応するアノテーションが存在しない場合はnull
	 */
	public String getErrorCode(String annotationType) {
		if (this.annotations.size() == 0) {
			return null;
		}
		return this.annotations.get(annotationType);
	}
}
