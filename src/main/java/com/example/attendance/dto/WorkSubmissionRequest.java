package com.example.attendance.dto;

import lombok.Data;

/**
 * 勤怠確定（申請）画面および給与計算の結果データを保持するデータ転送オブジェクト（DTO）。
 *
 * @author kato
 */
@Data
public class WorkSubmissionRequest {

	/** 月ごとの総平日日数（祝日を除く） */
	private int mustDay;

	/** 社員ごとの対象月の実出社日数 */
	private int attendanceDay;

	/** 対象月に有給休暇を取得した日数 */
	private int paidHoliDay;

	/** 当月の総残業時間（時間単位の小数表記） */
	private double overTime;

	/** * 当月の総深夜作業時間（時間単位の小数表記） 
	 * <p>💡【修正】タイポ（lateNigthTime）を正しいスペル（lateNightTime）に補正しました。</p>
	 */
	private double lateNightTime;

	/** 当月の総休日作業時間（時間単位の小数表記） */
	private double holidayTime;

	/** 計算された当月の確定支給総額（給与） */
	private int salary;

	/**
	 * デフォルトコンストラクタ。
	 */
	public WorkSubmissionRequest() {
	}
}