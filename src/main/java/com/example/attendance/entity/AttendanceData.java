package com.example.attendance.entity;

import java.time.format.DateTimeFormatter;

import lombok.Data;

/**
 * 1日ごとの勤務実績データを保持するエンティティクラス。
 * <p>
 * <b>【ShainData との違い】</b><br>
 * ・{@code ShainData}: 社員の「マスター情報」（ログインID、パスワード、氏名など）を管理し、
 * 主に認証やセッション保持に使用されます。<br>
 * ・{@code AttendanceData}: その社員が「いつ、何時間働いたか」という
 * 「日々の勤務実績情報」（出退勤時刻、勤務分数、残業時間など）を管理します。<br>
 * つまり、{@code ShainData}（誰が）に紐づく、日々の活動を記録するのがこの {@code AttendanceData} です。
 * </p>
 *
 * @author Hagiwara
 */
@Data
public class AttendanceData {
	/** 出勤日のフォーマット*/
	public static final DateTimeFormatter WORK_DAY_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

	/** 定時の勤務時間(分)*/
	public static final int REGULAR_WORK_TIME = 480;
	
	/** 社員を一意に識別するための社員ID（ShainDataのIDと紐づきます） */
	private int shainId;

	/** 出勤日（フォーマット済みの文字列 例: "2026/06/02"） */
	private String workDay;

	/** 出勤時刻（時:分） */
	private String startTime;

	/** 退勤時刻（時:分） */
	private String endTime;

	/** 当日の総勤務時間（分数単位） */
	private long minutes;

	/** 勤務に関する備考（有給申請や遅刻理由など） */
	private String note;

	/** 出勤または休みのフラグ（true: 休み、false: 出勤日） */
	private boolean breakDay;

	/** 定時を超過した残業時間（フォーマット済みの文字列 例: " 1:30"） */
	private String overTime;

	/** 出勤日の曜日（例: "月", "火"） */
	private String dayOfWeek;
	
	/**祝日の場合、その種類*/
	private String holiday;
}
