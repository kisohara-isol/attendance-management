package com.example.attendance.util.nationalholiday;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;

import com.example.attendance.util.DateTimeUtil;

/**
 * 「国民の祝日に関する法律」第3条の第2項および第3項によって定義された、<br>
 * 国民の祝日の存在によって休日となった日=「国民の休日」を表す抽象クラス
 */
public abstract sealed class PublicHolidayRelatedDay permits SubstituteHoliday, InBetweenHoliday {
	/**
	 *  この休日が設けられる原因となった祝日。<br>
	 *  1つ(振り替え休日)もしくは2つ(祝日の間の休日)
	 */
	final Set<NationalHoliday> RERATED_HOLIDAYS;
	/**日付*/
	final LocalDate DATE;

	/**
	 * コンストラクタ
	 * @param date この休日の日付
	 * @param rerateds この休日が設けられる原因となった祝日
	 */
	public PublicHolidayRelatedDay(LocalDate date, NationalHoliday... rerateds) {
		this.DATE = date;
		this.RERATED_HOLIDAYS = Set.of(rerateds);
	}

	/**
	 * 日付のgetter
	 * @return 日付
	 */
	public LocalDate getDate() {
		return this.DATE;
	}

	/**
	 * この休日に関連する祝日のgetter
	 * @return 関連する祝日(1~2個)
	 */
	public Set<NationalHoliday> getReratedHolidays() {
		return this.RERATED_HOLIDAYS;
	}

	/**
	 * 名前のgetter
	 * @return 名前
	 */
	public abstract String getName();

	/**
	 *Equals()のオーバーライド。日付で等価判定を行う。
	 *@param obj 比較先
	 *@return 日付が同じPublicHolidayRelatedDayならtrue
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof PublicHolidayRelatedDay holiday) {
			if (this.getDate().equals(holiday.getDate())) {
				return true;
			}
		}
		return false;
	}

	/**
	 *hashCode()のオーバーライド。
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.getDate());
	}

	/**
	 * toString()のオーバーライド
	 *
	 */
	@Override
	public String toString() {
		return this.getName() + "[日付:" + this.getDate() + "]";
	}

	/**
	 * ある年の全ての「国民の休日」を日付順で格納した不変リストを返却する。
	 * @param year 対象の年
	 * @return その年における「国民の休日」の不変リスト
	 */
	public static List<PublicHolidayRelatedDay> getAllReratedDay(Year year) {
		var result = new ArrayList<PublicHolidayRelatedDay>();
		var holidayMap = NationalHoliday.getDateToHolidayMap(year);

		holidayMap.forEach((holidayDate, holiday) -> {
			LocalDate afterHoliday = holidayDate.plusDays(1);

			if (holidayDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
				//日曜日なら振り替え休日を作成
				while (afterHoliday.getDayOfWeek().equals(DayOfWeek.SUNDAY) || holidayMap.containsKey(afterHoliday)) {
					//増加した日付が「日曜日」または「祝日」ではなくなるまで増加
					afterHoliday = afterHoliday.plusDays(1);
				}
				result.add(new SubstituteHoliday(afterHoliday, holiday));

			} else if (!afterHoliday.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
				//・「ある祝日と次の祝日の差が2日」、かつ
				//・「ある祝日の次の日(=間の日)が日曜日でない」なら「祝日の間の休日」を作成
				LocalDate nextHolidayDate = holidayMap.higherKey(holidayDate);
				if (nextHolidayDate != null && ChronoUnit.DAYS.between(holidayDate, nextHolidayDate) == 2) {
					result.add(new InBetweenHoliday(afterHoliday, holiday, holidayMap.get(nextHolidayDate)));
				}
			}
		});
		//不変リストにして返却
		return Collections.unmodifiableList(result);
	}

	/**
	 * ある年の全ての「国民の休日」を、その日付をkeyとして日付順に格納した不変Mapとして返却する。
	 * @param year 対象の年
	 * @return その年における「国民の休日」を、{日付=国民の休日}の形で格納した不変Map
	 */
	public static NavigableMap<LocalDate, PublicHolidayRelatedDay> getAllReratedHolydaysMap(Year year) {
		return DateTimeUtil.convertToNavigableMapByLocalDate(
				PublicHolidayRelatedDay.getAllReratedDay(year),
				x -> x.getDate());
	}
}
