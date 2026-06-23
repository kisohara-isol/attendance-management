package com.example.attendance.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
/**
 * 日本の祝日および休日（振替休日・国民の休日）を判定・管理するEnumクラスです。
 * <p>
 * 内閣府の「国民の祝日に関する法律」に準拠し、以下の4つのタイプに分類して判定を行います：
 * </p>
 * <ul>
 * <li><b>fixed:</b> 日付固定の祝日（例：元日、建国記念の日など）</li>
 * <li><b>monday:</b> ハッピーマンデー制度に基づく特定の月曜日（例：成人の日、海の日など）</li>
 * <li><b>spring:</b> 簡易計算式を用いた「春分の日」の自動判定</li>
 * <li><b>autumn:</b> 簡易計算式を用いた「秋分の日」の自動判定</li>
 * </ul>
 * <p>
 * さらに、祝日が日曜日の場合に翌日以降を休日とする「振替休日」や、
 * 前後を祝日に挟まれた平日を休日とする「国民の休日」の判定ロジックも内包しています。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum Holiday {
	NEWYEAR("元日", "fixed", 1, 1, 0), ADULT("成人の日", "monday", 1, 0, 2), FOUNDATION("建国記念の日", "fixed", 2, 11, 0),
	BIRTHDAY("天皇誕生日", "fixed", 2, 23, 0), SPRING("春分の日", "spring", 3, 0, 0), SHOWA("昭和の日", "fixed", 4, 29, 0),
	CONSTITUTION("憲法記念日", "fixed", 5, 3, 0), GREEN("みどりの日", "fixed", 5, 4, 0), CHILDREN("こどもの日", "fixed", 5, 5, 0),
	SEA("海の日", "monday", 7, 0, 3), MOUNTAIN("山の日", "fixed", 8, 11, 0), AGED("敬老の日", "monday", 9, 0, 3),
	AUTUMN("秋分の日", "autumn", 9, 0, 0), SPORTS("スポーツの日", "monday", 10, 0, 2), CULTURE("文化の日", "fixed", 11, 3, 0),
	THANKSGIVING("勤労感謝の日", "fixed", 11, 23, 0);

	/** 祝日の名前 */
	private String name;

	/**
	 * 判定タイプ fixed: 日付固定祝日<br>
	 * monday: 第何月曜日<br>
	 * spring: 春分の日<br>
	 * autumn: 秋分の日<br>
	 */
	private String type;

	/** 月 */
	private int month;

	/** 日付型: 日 */
	private int dayOfMonth;

	/** 第何月曜日型: 第何か */
	private int weekOfMonth;

	/**
	 * 全ての祝日を取得
	 */
	public static List<Holiday> getAllHoliday() {

		return Stream.of(values()) // values() は、Enumクラスが標準で持っているメソッド Enum内の全ての要素を配列にする
				.collect(Collectors.toList()); // List<Holiday>に集約
	}
	
	/**
	 * 振替休日や国民の休日を考慮しない、日本本来の固有の祝日であるかを判定します。
	 *
	 * @param date 判定対象の日付
	 * @return 該当する祝日の名前。祝日でない場合は {@code null}
	 */
	public static String judgePureHoliday(LocalDate date) {
		List<Holiday> allHoliday = Holiday.getAllHoliday();

		// 祝日のリストの中に一致するものがあるか調べる
		for (Holiday h : allHoliday) {
			// 日にち固定の祝日かどうかを調べる
			if (h.getType().equals("fixed")) {
				if (h.getMonth() == date.getMonthValue() && h.getDayOfMonth() == date.getDayOfMonth()) {
					return h.getName();
				}

				// 第何月曜日型かどうかを調べる
			} else if (h.getType().equals("monday")) {
				if (h.getMonth() == date.getMonthValue()) {
					LocalDate monday = LocalDate.of(date.getYear(), h.getMonth(), 1)
							.with(TemporalAdjusters.dayOfWeekInMonth(h.getWeekOfMonth(), DayOfWeek.MONDAY));
					if (monday.getDayOfMonth() == date.getDayOfMonth()) {
						return h.getName();
					}
				}

				// 春分の日かどうかを調べる
			} else if (h.getType().equals("spring")) {
				if (h.getMonth() == date.getMonthValue()) {
					int springDay = (int) (20.8431 + 0.242194 * (date.getYear() - 1980)
							- (int) ((date.getYear() - 1980) / 4));
					if (springDay == date.getDayOfMonth()) {
						return h.getName();
					}
				}

				 //秋分の日かどうかを調べる
			} else if (h.getType().equals("autumn")){
				if (h.getMonth() == date.getMonthValue()) {
					int autumnDay = (int) (23.2488 + 0.242194 * (date.getYear() - 1980)
							- (int) ((date.getYear() - 1980) / 4));
					if (autumnDay == date.getDayOfMonth()) {
						return h.getName();
					}
				}
			}
		}
		return null;
		
	}

	/**
	 * 指定された日付が「祝日」「振替休日」「国民の休日」のいずれかであるかを総合的に判定します。
	 * <p>
	 * 判定は以下の優先順位で行われます：
	 * </p>
	 * <ol>
	 * <li>本来の固定祝日・移動祝日の判定 ({@link #judgePureHoliday(LocalDate)})</li>
	 * <li>祝日が日曜日に重なった場合の「振替休日」の判定（翌日以降の平日へスライド）</li>
	 * <li>平日が2つの祝日に挟まれた場合の「国民の休日」の判定（例：5月4日が平日の場合、5月3日と5日の間に挟まれるため休日となる）</li>
	 * </ol>
	 * * @param date 判定対象の日付 ({@link LocalDate})
	 * @return 休日名（例："元日", "振替休日", "国民の休日"）。祝日または休日でない場合は {@code null}
	 */
	public static String judgeHoliday(LocalDate date) {
		// 本来の休日の取得
		String baseHolidayName = judgePureHoliday(date);
		if (baseHolidayName != null) {
			return baseHolidayName;
		}

		// 振替休日の判定
		int year = date.getYear();
		int month = date.getMonthValue();

		YearMonth ym = YearMonth.of(year, month);
		Map<LocalDate, Boolean> fixedHolidayMap = new HashMap<>();

		// その月の本来の祝日をmapにセットする
		for (int d = 1; d <= ym.lengthOfMonth(); d++) {
			LocalDate checkDate = LocalDate.of(year, month, d);
			if (judgePureHoliday(checkDate) != null) {
				fixedHolidayMap.put(checkDate, true);
			}
		}
		// 本来の祝日を必要に応じてスライドさせる
		for (int d = 1; d <= ym.lengthOfMonth(); d++) {
			LocalDate checkDate = LocalDate.of(year, month, d);

			// 「日曜日」かつ「本来の祝日」のとき、翌日以降へスライド
			if (checkDate.getDayOfWeek() == DayOfWeek.SUNDAY && fixedHolidayMap.containsKey(checkDate)) {
				LocalDate target = checkDate.plusDays(1);

				// 移動先が土日、またはすでに本来の祝日なら、さらに次の日へスライド
				while (target.getDayOfWeek() == DayOfWeek.SATURDAY || target.getDayOfWeek() == DayOfWeek.SUNDAY
						|| fixedHolidayMap.containsKey(target)) {
					target = target.plusDays(1);
				}

				// スライドした結果が、調べたい引数のdateと完全一致したら「振替休日」確定
				if (target.equals(date)) {
					return "振替休日";
				}
			}
		}
		// 国民の日の判定
		// 引数の前後の日を取得
		LocalDate prev = date.minusDays(1);
		LocalDate next = date.plusDays(1);

		// 現在の日が土日ではなく、かつ前後の日がどちらも「本来の祝日」であれば
		if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
			if (judgePureHoliday(prev) != null && judgePureHoliday(next) != null) {
				return "国民の休日";
			}
		}

		// 全てに当てはまらなかったらnull
		return null;
	}

}
