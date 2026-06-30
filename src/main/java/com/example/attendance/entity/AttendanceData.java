package com.example.attendance.entity;

import lombok.Data;

/**
 * 1日ごとの勤務実績データを保持するエンティティクラス。
 * <p>
 * <b>【ShainData との違い】</b><br>
 * ・{@link ShainData}: 社員の「マスター情報」（ログインID、氏名、アカウントの停止状態など）を管理します。<br>
 * ・{@link AttendanceData}: その社員に紐づく「日々の動的な活動実績」（出退勤時刻、勤務分数、残業時間など）を1日1レコードとして管理します。
 * </p>
 *
 * @author Hagiwara
 */
@Data
public class AttendanceData {
	
	/** 社員を一意に識別するための社員ID（ShainDataの主キーと紐づきます） */
	private int shainId;

	/** 出勤日（フォーマット済みの文字列 例: "2026/06/02" または "2"） */
	private String workDay;

	/** 出勤時刻（フォーマット済みの文字列 例: "09:00"） */
	private String startTime;

	/** 退勤時刻（フォーマット済みの文字列 例: "18:00"） */
	private String endTime;

	/** 当日の総勤務時間（分数単位、給与計算のベースとなる数値） */
	private long minutes;

	/** 勤務に関する備考・申請理由（例: "有給", "体調不良による遅刻" など） */
	private String note;

	/** 休日フラグ（true: 休み、false: 出勤日） */
	private boolean breakDay;

	/** 定時（8時間）を超過した残業時間（フォーマット済みの文字列 例: "1:30"） */
	private String overTime;

	/** 出勤日の曜日（例: "月", "火"） */
	private String dayOfWeek;
}