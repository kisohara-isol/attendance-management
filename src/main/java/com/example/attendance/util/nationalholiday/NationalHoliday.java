package com.example.attendance.util.nationalholiday;

import static java.util.Map.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.example.attendance.util.DateTimeUtil;

/**
 * 「国民の祝日に関する法律」に基づく日本の国民の祝日を列挙したEnumクラス
 * <p>参照:https://www8.cao.go.jp/chosei/shukujitsu/gaiyou.html</p>
 */
public enum NationalHoliday {
	/**元日:毎年1月1日*/
	NER_YEARS_DAY("元日") {
		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			return DateTimeUtil.atYearOpt(year, MonthDay.of(Month.JANUARY, 1));
		}
	},
	/**成人の日:1月の第2月曜日*/
	COMING_OF_AGE_DAY("成人の日") {
		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			var date = year.atMonth(Month.JANUARY).atDay(1) //Januaryの1日でLocalDateを作成
					.with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.MONDAY)); //第二月曜日に調整
			return Optional.of(date);
		}
	},
	/**建国記念日:2月11日*/
	NATIONAL_FOUNDATION_DAY("建国記念日") {
		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			return DateTimeUtil.atYearOpt(year, MonthDay.of(Month.FEBRUARY, 11));
		}
	},
	/**天皇誕生日:<b>令和においては</b>2月23日*/
	EMPERORS_BIRTHDAY("天皇誕生日") {
		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			return DateTimeUtil.atYearOpt(year, MonthDay.of(Month.FEBRUARY, 23));
		}
	},
	/**
	 * 春分の日:国立天文台が毎年定める春分日
	 * 今回は3月21日固定
	 */
	VERNAL_EQUINOX_DAY("春分の日") {
		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			MonthDay vernalEquinox = MonthDay.of(Month.MARCH, 21);
			return DateTimeUtil.atYearOpt(year, vernalEquinox);
		}
	},
	/**昭和の日:4月29日*/
	SHOWA_DAY("昭和の日") {
		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			return DateTimeUtil.atYearOpt(year, MonthDay.of(Month.APRIL, 29));
		}
	},
	/**憲法記念日:5月3日*/
	CONSTITUTION_DAY("憲法記念日") {
		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			return DateTimeUtil.atYearOpt(year, MonthDay.of(Month.MAY, 3));
		}
	},
	/**みどりの日:5月4日*/
	GREENERY_DAY("みどりの日") {
		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			return DateTimeUtil.atYearOpt(year, MonthDay.of(Month.MAY, 4));
		}
	},
	/**こどもの日:5月5日*/
	CHILDRENS_DAY("こどもの日") {
		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			return DateTimeUtil.atYearOpt(year, MonthDay.of(Month.MAY, 5));
		}
	},
	/**海の日:7月の第3月曜日*/
	MARINE_DAY("海の日") {
		private final Map<Year, MonthDay> EXCEPTIONAL_DATES = Map.ofEntries(
				entry(Year.of(2020), MonthDay.of(Month.JULY, 23)),
				entry(Year.of(2021), MonthDay.of(Month.JULY, 22)));

		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			if (EXCEPTIONAL_DATES.containsKey(year)) {
				//もし例外的に祝日が移動した年ならば、該当の日付を返す
				return DateTimeUtil.atYearOpt(year, EXCEPTIONAL_DATES.get(year));
			}
			var date = year.atMonth(Month.JULY).atDay(1)
					.with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY));
			return Optional.of(date);
		}
	},
	/**山の日:8月11日*/
	MOUNTAIN_DAY("山の日") {
		private final Map<Year, MonthDay> EXCEPTIONAL_DATES = Map.ofEntries(
				entry(Year.of(2020), MonthDay.of(Month.AUGUST, 10)),
				entry(Year.of(2021), MonthDay.of(Month.AUGUST, 8)));

		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			if (EXCEPTIONAL_DATES.containsKey(year)) {
				//もし例外的に祝日が移動した年ならば、該当の日付を返す
				return DateTimeUtil.atYearOpt(year, EXCEPTIONAL_DATES.get(year));
			}
			return DateTimeUtil.atYearOpt(year, MonthDay.of(Month.AUGUST, 11));
		}
	},
	/**敬老の日:9月の第3月曜日*/
	RESPECT_FOR_THE_AGED_DAY("敬老の日") {
		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			var date = year.atMonth(Month.SEPTEMBER).atDay(1)
					.with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY));
			return Optional.of(date);
		}
	},
	/**
	 * 秋分の日:国立天文台が毎年定める秋分日
	 * 今回は9月24日固定
	 */
	AUTUMNAL_EQUINOX_DAY("秋分の日") {
		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			MonthDay autamnalEquinox = MonthDay.of(Month.SEPTEMBER, 24);
			return DateTimeUtil.atYearOpt(year, autamnalEquinox);
		}
	},
	/**スポーツの日(旧「体育の日」):10月の第2月曜日*/
	SPORTS_DAY("スポーツの日") {
		private final Map<Year, MonthDay> EXCEPTIONAL_DATES = Map.ofEntries(
				entry(Year.of(2020), MonthDay.of(Month.JULY, 24)),
				entry(Year.of(2021), MonthDay.of(Month.JULY, 23)));

		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			if (EXCEPTIONAL_DATES.containsKey(year)) {
				//もし例外的に祝日が移動した年ならば、該当の日付を返す
				return DateTimeUtil.atYearOpt(year, EXCEPTIONAL_DATES.get(year));
			}
			var date = year.atMonth(Month.OCTOBER).atDay(1)
					.with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.MONDAY));
			return Optional.of(date);
		}
	},
	/**文化の日:11月3日*/
	CULTURE_DAY("文化の日") {
		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			return DateTimeUtil.atYearOpt(year, MonthDay.of(Month.NOVEMBER, 3));
		}
	},
	/**勤労感謝の日:11月23日*/
	LABOR_THANKSGIVING_DAY("勤労感謝の日") {
		@Override
		public Optional<LocalDate> getDateOfTheYear(Year year) {
			return DateTimeUtil.atYearOpt(year, MonthDay.of(Month.NOVEMBER, 23));
		}
	};

	private NationalHoliday(String japaneseName) {
		this.japaneseName = japaneseName;
	}

	/**
	 * 年を指定すると、このEnum列挙子の表す祝日に該当するLocalDateを返す。
	 * @param year 対象の年
	 * @return その年におけるこの祝日の日付を保持するOptional<br>
	 * 2015年までの「山の日」など、無効な日付の場合は空のOptionalで帰ってくる(注:今回は該当なし)
	 */
	public abstract Optional<LocalDate> getDateOfTheYear(Year year);

	/**この祝日の日本語名*/
	private String japaneseName;

	/**
	 * 日本語名のgetter
	 * @return 日本語名
	 */
	public String getJapaneseName() {
		return this.japaneseName;
	}

	/**
	 * 年を指定することで、その年に存在する国民の祝日一覧を取得する。
	 * @param year 年
	 * @return 祝日の不変List。早い順でsortされている。
	 */
	public static List<NationalHoliday> getSortedHolidayListOfYear(Year year) {
		var result = Stream.of(NationalHoliday.values())
				.filter(x -> x.getDateOfTheYear(year).isPresent())
				.sorted((x, y) -> x.getDateOfTheYear(year).get().compareTo(y.getDateOfTheYear(year).get()))
				.toList();
		return result;
	}

	/**
	 * 年を指定することで、キーが国民の祝日で、値がその日付である祝日一覧を取得する。
	 * @param year 年
	 * @return {祝日,日付}のMap
	 */
	public static Map<NationalHoliday, LocalDate> getHolidayToDateMap(Year year) {
		var map = NationalHoliday.getSortedHolidayListOfYear(year).stream()
				.collect(Collectors.toMap(x -> x, x -> x.getDateOfTheYear(year).get()));
		return map;
	}

	/**
	 * 年を指定することで、キーが日付で、値がその日の国民の祝日である、日付によって順序付けされた祝日一覧を取得する。
	 * @param year 年
	 * @return {日付,祝日}のMap
	 */
	public static NavigableMap<LocalDate, NationalHoliday> getDateToHolidayMap(Year year) {
		return DateTimeUtil.convertToNavigableMapByLocalDate(
				NationalHoliday.getSortedHolidayListOfYear(year),
				x -> x.getDateOfTheYear(year).get());
	}

	/**
	 * 日本名から、対応する国民の祝日を取得する
	 * @param japaneseName 日本名
	 * @return マッチすれば祝日が入った、しなければ空のOptional
	 */
	public static Optional<NationalHoliday> fromJapaneseName(String japaneseName) {
		NationalHoliday result = Stream.of(NationalHoliday.values())
				.filter(x -> x.getJapaneseName().equals(japaneseName))
				.findFirst()
				.orElseGet(() -> {
					if ("体育の日".equals(japaneseName)) {
						//「体育の日」が渡された場合にはSPORTS_DAYを返す
						return NationalHoliday.SPORTS_DAY;
					} else {
						return null;
					}
				});
		return Optional.ofNullable(result);
	}
}
