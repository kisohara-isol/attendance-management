package com.example.attendance.service;

import com.example.attendance.dto.CreateWorkRequest;
import com.example.attendance.entity.ShainData;

/**
 * 勤務登録確認画面におけるビジネスロジックを定義するサービスインターフェース。
 * <p>
 * 画面からの入力値の加工（型変換や初期値の設定）および、
 * セッションからのログイン社員情報の取得を行い、データベースへの永続化処理を統括します。
 * @author Soeda
 * </p>
 */
public interface WorkConfirmService {

	/**
	 * 勤務確認画面で「追加」ボタンが押された際、入力された勤怠データをデータベースに登録します。
	 * <p>
	 * <b>【主な内部処理内容】</b>
	 * <ul>
	 * <li>{@code session} からログイン中の社員情報（{@code ShainData}）を安全に取得します。</li>
	 * <li>{@code createWorkRequest} 内の文字列型（String）の時間データを、適切な時間型（{@code LocalTime}）へ変換します。</li>
	 * <li>出勤時間が未入力・「休み」の場合、および退勤時間が未入力の場合は、自動的に初期値として {@code 00:00} をセットします。</li>
	 * </ul>
	 * </p>
	 *
	 * @param createWorkRequest 勤務登録画面から送信された入力値（日付、出退勤時間、備考など）が格納されたDTO
	 * @param session           ログインユーザーのセッション情報を管理するHTTPセッションオブジェクト
	 */
	void insertAttendanceData(CreateWorkRequest createWorkRequest, ShainData shain);

}
