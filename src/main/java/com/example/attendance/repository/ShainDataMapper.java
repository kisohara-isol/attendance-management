package com.example.attendance.repository;

import java.time.LocalDate;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // ⭕ MyBatis用のアノテーションに変更しました
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.attendance.entity.ShainData;

/**
 * {@link ShainData} および勤怠データに関するデータベース操作を行うマッパーインターフェース。
 * @author Soeda
 * </p>
 */
@Mapper
public interface ShainDataMapper {

	/**
	 * ログイン時に、入力されたログインIDとパスワードに一致する社員情報を取得します。
	 *
	 * @param loginId  画面から入力されたログインID
	 * @param password 画面から入力されたパスワード
	 * @return 一致する社員データ（見つからない場合は {@x.code null}）
	 */
	@Select("SELECT * FROM shain_data WHERE login_id = #{loginId} AND password = #{password}")
	ShainData selectShainData(@Param("loginId") String loginId, @Param("password") String password);

	/**
	 * 指定されたログインIDを基に、社員情報を1件取得します。
	 * <p>
	 * 主にアカウントの存在チェックや、ログイン失敗時のロック処理（情報の再取得）で使用します。
	 * </p>
	 *
	 * @param loginId 検索対象のログインID
	 * @return 一致する社員データ（見つからない場合は {@x.code null}）
	 */
	@Select("SELECT * FROM shain_data WHERE login_id = #{loginId}")
	ShainData selectShainById(@Param("loginId") String loginId);

	/**
	 * 勤務確認画面で「追加」ボタンが押された際、勤怠データをテーブルに新規登録（インサート）します。
	 *
	 * @param shainId   社員ID
	 * @param workDay   出勤日
	 * @param startTime 出勤時間（未入力や休みの場合は 00:00）
	 * @param endTime   退勤時間（未入力の場合は 00:00）
	 * @param note      備考
	 */
	/**
	 * 勤務確認画面で「追加」ボタンが押された際、勤怠データをテーブルに新規登録（インサート）します。
	 */
	@Insert("INSERT INTO attendance_table (shain_id, work_day, start_time, end_time, note) " +
			"VALUES (#{shainId}, #{workDay}, #{startTime}, #{endTime}, #{note})")
	// 💡 【修正】消えていた引数を復活させ、時間を LocalTime から String に変更しました
	void insertAttendanceData(
			@Param("shainId") int shainId,
			@Param("workDay") LocalDate workDay,
			@Param("startTime") String startTime, // ⭕ String型に変更
			@Param("endTime") String endTime, // ⭕ String型に変更
			@Param("note") String note);

	/**
	 * 既存の勤怠データを更新（アップデート）します。
	 */
	@Update("UPDATE attendance_table SET start_time = #{startTime}, end_time = #{endTime}, note = #{note} " +
			"WHERE shain_id = #{shainId} AND work_day = #{workDay}")
	// 💡 【修正】時間を LocalTime から String に変更しました
	void updateAttendanceData(
			@Param("shainId") int shainId,
			@Param("workDay") LocalDate workDay,
			@Param("startTime") String startTime, // ⭕ String型に変更
			@Param("endTime") String endTime, // ⭕ String型に変更
			@Param("note") String note);

	/**
	 * すでにデータが存在するか確認するためのカウントSQL
	 */
	@Select("SELECT COUNT(*) FROM attendance_table WHERE shain_id = #{shainId} AND work_day = #{workDay}")
	int countAttendanceData(@Param("shainId") int shainId, @Param("workDay") LocalDate workDay);

	/**
	 * 引数で渡された社員オブジェクトの情報に基づき、データベースの社員データを更新（アップデート）します。
	 * <p>
	 * 主に、ログイン失敗が3回に達した際のアカウントロック（{@x.code stop_flg = 1} への更新）などで使用します。
	 * </p>
	 *
	 * @param shain 更新対象のデータ（ID、新しい停止フラグなど）が入った社員エンティティ
	 */
	@Update("UPDATE shain_data SET stop_flg = #{stopFlg} WHERE login_id = #{loginId}")
	void updateShainData(ShainData shain);

	/**
	 * 引数で渡された社員IDに対応するアカウントのstop_flgを0にリセット(アップデート)します。
	 * @param shainId 対象となる社員ID
	 * @return 更新に成功した数<br>正しく実行されれば1であることが期待される
	 */
	@Update("UPDATE shain_data SET stop_flg = 0 AND failure_count = 0 WHERE shain_id = #{shainId} AND stop_flg = 1 AND failure_count =3")
	int resetStopFlugByShainId(@Param("shainId") int shainId);

	/**
	 * 社員ごとの給料計算明細を取得するマッパー
	 * 社員IDから取得
	 * */
	@Select("SELECT * FROM  salary_data WHERE shain_id = #{shainId}")
	Map<String, Object> selectSalaryById(@Param("shainId") int shainId);

	/**
	 * 引数で渡された社員IDに対応するアカウントのfailure_countを更新します。
	 * ログインを一度失敗するごとに１づつ増やす
	 * */
	@Update("UPDATE shain_data SET failure_count = #{failureCount} WHERE login_id = #{loginId}")
	int updateFailureCount(ShainData shain);

}