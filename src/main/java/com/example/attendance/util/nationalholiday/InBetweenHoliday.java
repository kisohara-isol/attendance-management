package com.example.attendance.util.nationalholiday;

import java.time.LocalDate;

/**
 * 「国民の祝日に関する法律」第3条の第3項、
 * <blockquote>その前日及び翌日が「国民の祝日」である日（「国民の祝日」でない日に限る。）は、休日とする。</blockquote>
 * に基づく休日。<br>
 * つまり、祝日に挟まれた日
 */
public final class InBetweenHoliday extends PublicHolidayRelatedDay {
	/**名前。正式な呼称が無かったため適当なもの*/
	private final String NAME = "祝日の間の休日";

	/**
	 * コンストラクタ
	 * @param date 日付
	 * @param rerated1 この休日が設けられる原因となった祝日の一つ(順不同)
	 * @param rerated2 同上
	 */
	public InBetweenHoliday(LocalDate date, NationalHoliday rerated1, NationalHoliday rerated2) {
		super(date, rerated1, rerated2);
	}

	@Override
	public String getName() {
		return this.NAME;
	}
}
