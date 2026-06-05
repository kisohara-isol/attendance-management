package com.example.attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.dto.CreateWorkRequest;
import com.example.attendance.entity.ShainData;
import com.example.attendance.repository.ShainDataMapper;

/**
 * 勤務登録確認画面におけるビジネスロジックを提供するサービス実装クラス。
 * <p>
 * コントローラーから受け取ったリクエストデータおよびセッション情報を基に、
 * データの検証・補正（型変換やデフォルト値の設定）を行い、マッパーを介してデータベースへ登録します。
 * @author Soeda
 * </p>
 */
@Service // ⭕ Springのサービスとして認識させるために必須のアノテーションです
public class WorkConfirmServiceImpl implements WorkConfirmService {

	/**
	 * 社員データおよび勤怠データ操作用のマッパー
	 */
	@Autowired
	private ShainDataMapper shainDataMapper;

	/**
	 * 画面から送られる時刻文字列（HHmm形式、例: "0900"）を {@link LocalTime} に変換するためのフォーマッター
	 */
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

	/**
	 * 勤務確認画面で「追加」ボタンが押された際、入力データを加工してデータベースに登録します。
	 * <p>
	 * <b>【処理フロー】</b>
	 * <ol>
	 * <li>セッションからログイン社員のID（{@code loginShain}）を取得します。</li>
	 * <li>画面から届いた出勤時間（文字列）を検証し、未入力や「休み」の場合は {@code 00:00} を、それ以外は {@link LocalTime} に変換します。</li>
	 * <li>画面から届いた退勤時間（文字列）を検証し、未入力の場合は {@code 00:00} を、それ以外は {@link LocalTime} に変換します。</li>
	 * <li>整合性を整えたデータを {@link ShainDataMapper#insertAttendanceData} に渡し、永続化します。</li>
	 * </ol>
	 * </p>
	 *
	 * @param request 勤務登録画面から送信された入力値（日付、出退勤時間、備考など）が格納されたDTO
	 * @param session ログインユーザーのセッション情報を管理するHTTPセッションオブジェクト
	 * @throws java.time.format.DateTimeParseException 時刻文字列のフォーマットが不正な場合
	 * 
	 */
	@Override
	@Transactional // ⭕ データベースへのインサート処理を伴うため、トランザクション管理を行います
	public void insertAttendanceData(CreateWorkRequest request, ShainData shain) {
		// 出勤日と備考を取得
		LocalDate workDay = request.getWorkDay();
		String note = request.getNote();

		// 1. 出勤時間の変換と初期化 (00:00)
		LocalTime startTime;
		if (request.getStartTime() == null || request.getStartTime().isEmpty() || request.getStartTime().equals("休み")) {
			// 空っぽ、または「休み」なら 00時00分 を代入
			startTime = LocalTime.MIN;
		} else {
			// 画面から「0900」などが入ってきたら、LocalTimeの 09:00 に変換
			startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
		}

		// 2. 退勤時間の変換と初期化 (00:00)
		LocalTime endTime;
		if (request.getEndTime() == null || "".equals(request.getEndTime())) {
			// 未入力なら 00時00分 を代入
			endTime = LocalTime.MIN;
		} else {
			// ⭕ 型エラーを修正：String型を正しく LocalTime にパースして代入します
			endTime = request.getEndTime();
		}

		// マッパーを呼び出してデータベースに登録
		shainDataMapper.insertAttendanceData(shain.getShainId(), workDay, startTime, endTime, note);
	}
}