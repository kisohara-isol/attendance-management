package com.example.attendance.util;

import static java.util.Map.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * dtoクラス"CreateWorkRequest"に存在するフィールドの列挙子。<br>
 * 各列挙子は、自身の表すフィールドが持つアノテーションを、{アノテーション名,エラーコード}のMapで保持する
 */
public enum CreateWorkRequestFeilds {
	/**出勤日(workDay)*/
	//ジェネリクスを用いる型(Entry<K,V>など)は配列にしたり可変長引数で渡したりすると型安全性が損なわれる
	//なので一度Listにしてあげる
	WORK_DAY(List.of(entry("typeMismatch", "W30003"), entry("NotNull", "W30001"))), //springの@DateTimeFormatはtypeMismatchの名で取得できる
	/**出勤時間(startTime)*/
	START_TIME(List.of(entry("NotEmpty", "W30002"), entry("Pattern", "W30004"))),
	/**退勤時間(endTIme)*/
	END_TIME(List.of(entry("typeMismatch", "W30006"), entry("HolidayCheck", "W30005"))),
	/**備考(note)*/
	NOTE(List.of());

	/**列挙子が持つ、{アノテーション名,エラーコード}のmap*/
	private Map<String, String> annotations;

	/**コンストラクタ
	 * @param entries 各フィールドのアノテーションとエラーコードがセットになったEntryのリスト
	 */
	private CreateWorkRequestFeilds(List<Map.Entry<String, String>> entries) {
		this.annotations = entries.stream()
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