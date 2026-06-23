package com.example.attendance.util.nationalholiday;

import java.time.LocalDate;

/**
 * 「国民の祝日に関する法律」第3条の第2項、
 * <p><blockquote>「国民の祝日」が日曜日に当たるときは、その日後においてその日に最も近い「国民の祝日」でない日を休日とする。</blockquote></p>
 * に基づく休日。いわゆる「振り替え休日」。
 */
public final class SubstituteHoliday extends PublicHolidayRelatedDay {
	/**名前。慣習的に用いられている「振り替え休日」の呼称*/
	private final String NAME = "振り替え休日";

	/**
	 * コンストラクタ
	 * @param date 日付
	 * @param rerated この休日が設けられる原因となった祝日
	 */
	public SubstituteHoliday(LocalDate date, NationalHoliday rerated) {
		super(date, rerated);
	}

	public String getName() {
		return this.NAME;
	}
}
