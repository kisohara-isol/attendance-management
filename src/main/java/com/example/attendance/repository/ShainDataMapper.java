package com.example.attendance.repository;

import java.time.LocalDate;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.attendance.entity.ShainData;

/**
 * 社員情報テーブル（shain_data）および一部勤怠実績に関するデータベース操作を司るMyBatisマッパーインターフェース。
 *
 * @author Soeda
 */
@Mapper
public interface ShainDataMapper {

	/**
	 * ログイン時に、入力されたログインIDとパスワードに一致する社員情報を取得します。
	 *
	 * @param loginId  画面から入力されたログインID
	 * @param password 画面から入力されたパスワード
	 * @return 一致する社員データ（見つからない場合は null）
	 */
	@Select("SELECT * FROM shain_data WHERE login_id = #{loginId} AND password = #{password}")
	ShainData selectShainData(@Param("loginId") String loginId, @Param("password") String password);

	/**
	 * 指定されたログインIDを基に、社員情報を1件取得します。
	 *
	 * @param loginId 検索対象のログインID
	 * @return 一致する社員データ（見つからない場合は null）
	 */
	@Select("SELECT * FROM shain_data WHERE login_id = #{loginId}")
	ShainData selectShainById(@Param("loginId") String loginId);

	/**
	 * 勤務確認画面で「追加」ボタンが押された際、勤怠データをテーブルに新規登録（インサート）します。
	 *
	 * @param shainId   社員ID
	 * @param workDay   出勤日
	 * @param startTime 出勤時間文字列（"0900" など）
	 * @param endTime   退勤時間文字列（"1800" など）
	 * @param note      備考
	 */
	@Insert("INSERT INTO attendance_table (shain_id, work_day, start_time, end_time, note) " +
			"VALUES (#{shainId}, #{workDay}, #{startTime}, #{endTime}, #{note})")
	void insertAttendanceData(
			@Param("shainId") int shainId,
			@Param("workDay") LocalDate workDay,
			@Param("startTime") String startTime,
			@Param("endTime") String endTime,
			@Param("note") String note);

	/**
	 * 既存の勤怠データを日付と社員IDをキーに更新（アップデート）します。
	 *
	 * @param shainId   社員ID
	 * @param workDay   対象の出勤日
	 * @param startTime 新しい出勤時間文字列
	 * @param endTime   新しい退勤時間文字列
	 * @param note      新しい備考
	 */
	@Update("UPDATE attendance_table SET start_time = #{startTime}, end_time = #{endTime}, note = #{note} " +
			"WHERE shain_id = #{shainId} AND work_day = #{workDay}")
	void updateAttendanceData(
			@Param("shainId") int shainId,
			@Param("workDay") LocalDate workDay,
			@Param("startTime") String startTime,
			@Param("endTime") String endTime,
			@Param("note") String note);

	/**
	 * 指定した社員の特定の日付の勤怠データが既に存在するか確認するための件数を取得します。
	 *
	 * @param shainId 社員ID
	 * @param workDay 確認対象の日付
	 * @return 存在するレコード件数（通常は 0 または 1）
	 */
	@Select("SELECT COUNT(*) FROM attendance_table WHERE shain_id = #{shainId} AND work_day = #{workDay}")
	int countAttendanceData(@Param("shainId") int shainId, @Param("workDay") LocalDate workDay);

	/**
	 * 対象社員の停止フラグ（stop_flg）を更新します（主にアカウントロックに使用）。
	 *
	 * @param shain 更新情報が含まれる社員エンティティオブジェクト
	 */
	@Update("UPDATE shain_data SET stop_flg = #{stopFlg} WHERE login_id = #{loginId}")
	void updateShainData(ShainData shain);

	/**
	 * 【致命的バグ修正】
	 * 対象アカウントのロックを解除し、失敗カウントをクリーンにリセットします。
	 * <p>
	 * 複数のSET句を AND で結んでいた不具合を、正しいSQL構文であるカンマ（,）区切りに修正しました。
	 * これにより、復旧ボタンを押した際に正常にデータがリセットされるようになります。
	 * </p>
	 *
	 * @param shainId ロック解除対象の社員ID
	 * @return 実際に更新されたレコード件数（成功時は 1）
	 */
	@Update("UPDATE shain_data SET stop_flg = 0, failure_count = 0 WHERE shain_id = #{shainId} AND stop_flg = 1")
	int resetStopFlugByShainId(@Param("shainId") int shainId);

	/**
	 * 指定された社員IDに対応するベース給与や各種手当倍率などのマスタ情報を取得します。
	 *
	 * @param shainId 社員ID
	 * @return 給与マスタデータを含むキー・値ペアのMap
	 */
	@Select("SELECT * FROM salary_data WHERE shain_id = #{shainId}")
	Map<String, Object> selectSalaryById(@Param("shainId") int shainId);

	/**
	 * 対象社員のログイン失敗回数（failure_count）を更新します。
	 *
	 * @param shain 新しい失敗回数がセットされた社員エンティティオブジェクト
	 * @return 更新成功件数
	 */
	@Update("UPDATE shain_data SET failure_count = #{failureCount} WHERE login_id = #{loginId}")
	int updateFailureCount(ShainData shain);
}