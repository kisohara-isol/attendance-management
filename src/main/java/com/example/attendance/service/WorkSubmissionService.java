package com.example.attendance.service;

import com.example.attendance.dto.WorkSubmissionRequest;
import com.example.attendance.entity.ShainData;

/**
 * 勤務表確定申請画面における、カレンダー日数や給与計算の集計ビジネスロジックを定義するサービスインターフェース。
 * <p>
 * 実装クラスでは、指定された年月を基に「祝日を除いた平日の必要日数」を算出し、
 * 日々の出退勤データから「実出社日数」「有給取得日数」「残業・深夜・休日作業時間」を
 * 網羅的に集計して、最終的な月給（支給総額）のシミュレーション計算を行います。
 * </p>
 *
 * @author kato (or Hagiwara)
 */
public interface WorkSubmissionService {

	/**
	 * 指定された年月の勤務実績を多角的に集計し、平日日数、実出社日数、各種手当を含む給与総額を算出してDTOに格納します。
	 * <p>
	 * 💡【重要ロジック】日中ではカバーしきれなかった「1時間休憩」の不足分を深夜労働時間から
	 * 自動的にマイナス補正（配分）する、極めて厳密な労働時間・手当計算処理を含みます。
	 * </p>
	 *
	 * @param workYear   集計対象の年 (YYYY)
	 * @param workMonth  集計対象の月 (MM)
	 * @param submission 計算・集計結果を格納して画面や後続処理に引き渡すためのDTOオブジェクト
	 * @param shain      給与単価や手当マスタを紐付けるための、現在処理対象となっている社員のマスターエンティティ
	 */
	void dateCounts(int workYear, int workMonth, WorkSubmissionRequest submission, ShainData shain);
	
	
}