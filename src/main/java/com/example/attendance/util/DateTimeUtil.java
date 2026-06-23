package com.example.attendance.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Period;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.example.attendance.util.nationalholiday.NationalHoliday;
import com.example.attendance.util.nationalholiday.PublicHolidayRelatedDay;

/**
 * LocalDateTime型をはじめとした、時間にまつわる処理のUtilクラス
 * @author kato
 */
public class DateTimeUtil {

	/**
	 * "25:00"のように、24時以上の時間を表す時間の文字列表現と日付のLocalDateを受け取り、<br>
	 * 両者を統合して正常なLocalDateTIme型に変換する。
	 * @param baseDay 基準となる日付
	 * @param timeOver24Hour 時間の文字列表現。24時間を超過して良い
	 * @param timeFormatter timeOver24Hourを解釈する方法を指定する
	 * @return baseDayとtimeOver24Hourを結合したLocalDateTime<br>
	 * 			24時間を超えていた場合、その分だけbaseDayの日数を進めて、時間部分は24時未満に丸める
	 */
	public static LocalDateTime correctOver24Hour(LocalDate baseDay, String timeOver24Hour,
			DateTimeFormatter timeFormatter) {
		TemporalAccessor timeAccessor = timeFormatter.withResolverStyle(ResolverStyle.LENIENT) //23:59:59を超えた時間を許容する
				.parse(timeOver24Hour);
		Period excessDays = timeAccessor.query(DateTimeFormatter.parsedExcessDays());
		LocalTime formattedTime = timeAccessor.query(LocalTime::from);
		return LocalDateTime.of(baseDay.plus(excessDays), formattedTime);
	}

	/**
	 * 与えられた「年」と「月日」を統合したLocaldate型の日付を保持するOptionalを返す
	 * @param year 年
	 * @param monthDay (年情報を持たない)月日
	 * @return Optional。引数がnullならば空、年と月日の統合に成功すればLocalDateが保持される
	 */
	public static Optional<LocalDate> atYearOpt(Year year, MonthDay monthDay) {
		try {
			return Optional.ofNullable(monthDay.atYear(year.getValue()));
		} catch (NullPointerException e) {
			return Optional.empty();
		}
	}

	/**
	 * 任意の型のオブジェクトによるリストと、その要素からLocalDateオブジェクトを作成するメソッドを渡すことで、<br>
	 * 各要素にメソッドを適用して得られるLocalDateをキーとした日付順の不変Mapを作成する。
	 * <p>キーとなるLocalDateが重複した場合は、新しく作成されたエントリを棄却する。</p>
	 * @param <T> 対象のオブジェクトの型
	 * @param localDateHolder オブジェクトのリスト
	 * @param convertMethod リストの要素1つを引数に取り、LocalDateを返すメソッド
	 * @return {LocalDate,オブジェクト}の形をとり、日付の昇順(早い順)で並べられた不変Map
	 */
	public static <T> NavigableMap<LocalDate, T> convertToNavigableMapByLocalDate(List<T> localDateHolder,
			Function<T, LocalDate> convertMethod) {
		TreeMap<LocalDate, T> result = localDateHolder.stream()
				.collect(Collectors.toMap(
						t -> convertMethod.apply(t),
						t -> t,
						(oldT, newT) -> oldT,
						TreeMap::new));
		return Collections.unmodifiableNavigableMap(result);
	}

	/**
	 * 渡された年をもとに、その年のすべての「国民の祝日」と関連する「国民の休日」を取得し、<br>
	 * {日付=(祝日もしくは休日の)名前}の形式をとった日付順の不変マップを返却する。
	 * @param year 対象の年
	 * @return {日付=名前}のMap
	 */
	public static NavigableMap<LocalDate, String> getAllHolidaysMapByLocalDate(Year year) {
		var holidayMap = NationalHoliday.getDateToHolidayMap(year);
		var relatedHolidayMap = PublicHolidayRelatedDay.getAllReratedHolydaysMap(year);
		var result = Stream
				.concat(
						holidayMap.entrySet().stream()
								.map(entry -> Map.entry(entry.getKey(), entry.getValue().getJapaneseName())),
						relatedHolidayMap.entrySet().stream()
								.map(entry -> Map.entry(entry.getKey(), entry.getValue().getName())))
				.collect(Collectors.toMap(
						x -> x.getKey(),
						x -> x.getValue(),
						(x, y) -> x,
						TreeMap::new));
		return Collections.unmodifiableNavigableMap(result);

	}

	/**
	 * 受け取った時間の文字列表現(hhmm形式)を、コロン区切りに変換する。
	 * <p>単純に4桁の整数文字列に対応。バリデーション等は他で行うこと。
	 * @param timeExpr hhmm形式による時間の文字列
	 * @return 正しい引数を渡していればhh:mmの文字列のOptional。それ以外は空のOptional。
	 */
	public static Optional<String> withColonStyle(String timeExpr) {
		final Pattern TIME_FORMAT = Pattern.compile("^(\\d{2})(\\d{2})$");
		var matcher = TIME_FORMAT.matcher(timeExpr);
		var result = matcher.find();
		if (!result) {
			return Optional.empty();
		}
		return Optional.of(matcher.group(1) + ":" + matcher.group(2));
	}
	

	/**
	 * 受け取った時間のコロン区切り文字列表現(hh:mm形式)を、コロン無しに変換する。
	 * <p>単純に2桁+2桁の整数文字列に対応。バリデーション等は他で行うこと。
	 * @param timeExpr hh:mm形式による時間の文字列
	 * @return 正しい引数を渡していればhhmmの文字列のOptional。それ以外は空のOptional。
	 */
	public static Optional<String> nonColonStyle(String timeExpr) {
		final Pattern TIME_FORMAT = Pattern.compile("^(\\d{2}):(\\d{2})$");
		var matcher = TIME_FORMAT.matcher(timeExpr);
		var result = matcher.find();
		if (!result) {
			return Optional.empty();
		}
		return Optional.of(matcher.group(1) + matcher.group(2));
	}
}
