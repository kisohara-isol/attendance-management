package com.example.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.example.attendance.dto.HolidayRule;
import com.example.attendance.entity.AttendanceData;

/**
 * 勤務表照会画面や給与計算ロジックで利用する、1ヶ月分の勤務実績リストおよび祝日情報の抽出を行うマッパーインターフェース。
 * <p>
 * MyBatisのアノテーション（{@link Select}）を使用し、勤怠実績（attendance_table）および
 * 祝日マスタ（holiday_rules）に対するデータ参照（セレクト文）を担当します。
 * </p>
 *
 * @author Soeda (or Hagiwara)
 */
@Mapper
public interface WorkTableMapper {

	/**
	 * 指定された社員IDおよび対象期間（1ヶ月分）に該当する勤怠実績データを取得します。
	 * <p>
	 * <b>【24時間超の丸め込みバグ回避仕様】</b><br>
	 * 徹夜勤務などで退勤時間が「31:30」のように24時間を超過したデータについて、MySQLのTIME型ドライバが
	 * 自動的に翌日時間に丸め込んでしまう挙動を完全に防止するため、SQL内部で {@code CAST(... AS CHAR)} を実行しています。<br>
	 * これにより、Javaのエンティティ（{@link AttendanceData}）のStringフィールドへ「"31:30"」等の生の文字列のまま安全にマッピングされます。
	 * </p>
	 *
	 * @param shainId        検索対象の社員ID
	 * @param workMonthStart 検索対象月の開始日 (例: 2026-06-01)
	 * @param workMonthEnd   検索対象月の翌月1日（未満条件用 例: 2026-07-01）
	 * @return 日付順（昇順）にソートされた、対象期間の勤務実績データのリスト
	 */
	@Select("SELECT "
			+ "  shain_id AS shainId, "
			+ "  work_day AS workDay, "
			+ "  CAST(start_time AS CHAR) AS startTime, "
			+ "  CAST(end_time AS CHAR) AS endTime, "
			+ "  note "
			+ "FROM attendance_table "
			+ "WHERE shain_id = #{shainId} "
			+ "  AND work_day >= #{workMonthStart} "
			+ "  AND work_day < #{workMonthEnd} "
			+ "ORDER BY work_day ASC")
	List<AttendanceData> selectAttendance(
			@Param("shainId") int shainId,
			@Param("workMonthStart") LocalDate workMonthStart,
			@Param("workMonthEnd") LocalDate workMonthEnd);

	/**
	 * 指定された月に関連するすべての祝日定義（日付、祝日名等）を祝日ルールマスタから取得します。
	 * <p>
	 * 取得された祝日リストは、サービス層で土日以外の休日判定や振替休日の自動計算を行うために利用されます。
	 * </p>
	 *
	 * @param month 検索対象の月 (1〜12)
	 * @return 対象月に該当する祝日ルールのリスト
	 */
	@Select("SELECT * FROM holiday_rules WHERE month = #{month}")
	List<HolidayRule> selectHoliday(int month);

	/**
	 * 指定された年月に、対象の社員の勤怠データがデータベースへ何件登録されているかをカウントします。
	 * <p>
	 * 引数で渡された年（String）と月（String）の文字列をSQL内部で結合（{@code CONCAT}）および整形（{@code LPAD}）し、
	 * その月の「初日（-01）」から「末日（{@code LAST_DAY}）」までの期間に含まれるレコード数を算出します。
	 * </p>
	 *
	 * @param shainId 対象の社員ID
	 * @param year    対象の年 (例: "2026")
	 * @param month   対象の月 (例: "6")
	 * @return 該当期間内に登録されている勤怠実績のレコード件数
	 */
	@Select("SELECT COUNT(*) FROM attendance_table " +
			"WHERE shain_id = #{shainId} " +
			"AND work_day BETWEEN " +
			"   STR_TO_DATE(CONCAT(#{year}, '-', LPAD(#{month}, 2, '0'), '-01'), '%Y-%m-%d') " +
			"AND LAST_DAY(STR_TO_DATE(CONCAT(#{year}, '-', LPAD(#{month}, 2, '0'), '-01'), '%Y-%m-%d'))")
	int countRegisteredDays(
			@Param("shainId") int shainId,
			@Param("year") String year,
			@Param("month") String month);
}