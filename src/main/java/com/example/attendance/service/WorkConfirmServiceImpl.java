package com.example.attendance.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.dto.CreateWorkRequest;
import com.example.attendance.entity.ShainData;
import com.example.attendance.repository.ShainDataMapper;

/**
 * 勤務登録確認画面における、最終永続化（インサート・アップデート）処理を統括するサービス実装クラス。
 *
 * @author Soeda
 */
@Service
public class WorkConfirmServiceImpl implements WorkConfirmService {

	/** 社員データおよび勤務データのマッピングを行うリポジトリクラス */
	@Autowired
	private ShainDataMapper shainDataMapper;

	/**
	 * 画面から送信された新規勤務登録リクエスト（CreateWorkRequest）を解析・クレンジングし、
	 * データベースの重複状況に応じてINSERTまたはUPDATEを適切に分岐実行します。
	 * <p>
	 * このメソッドは一連の処理が同一トランザクションとして実行され、例外検知時に自動ロールバックされます。
	 * </p>
	 *
	 * @param request 画面から送信された「出勤日」「新しい出退勤時刻」「備考」を含むDTO
	 * @param shain   操作を行っているログイン中の社員オブジェクト
	 * @throws DataAccessException データベース通信トラブル等の永続層例外が発生した場合
	 */
	@Override
	@Transactional
	public void insertAttendanceData(CreateWorkRequest request, ShainData shain) throws DataAccessException {
		LocalDate workDay = request.getWorkDay();
		String note = request.getNote();

		// 1. 時間のクレンジング・固定
		String startTime = startTimeFixed(request);
		String endTime = endTimeFixed(request, startTime);

		// 2. 存在チェック＆保存（インサート or アップデート）の実行
		upsertAttendance(shain.getShainId(), workDay, startTime, endTime, note);
	}

	/**
	 * 画面から送信された出勤時間をデータベース登録用の形式に変換します。
	 * <p>
	 * 出勤時間はString型で受け取り、休みの日は直接「休み」と入力されます。<br>
	 * 「休み」の場合は "00:00:00" に変換し、出勤日に関しては入力された時間に秒単位（:00）を補正・追加します。
	 * </p>
	 *
	 * @param request 画面から送信された「出勤日」「新しい出退勤時刻」「備考」を含むDTO
	 * @return MySQLのTIME型に対応した「HH:mm:00」形式の文字列
	 */
	private String startTimeFixed(CreateWorkRequest request) {
		String startTime = request.getStartTime();
		if ("休み".equals(startTime)) {
			startTime = "00:00:00";
		} else {
			// コロンや不要な文字を排除して数字だけにする (例: "09:00" -> "0900")
			startTime = startTime.replaceAll("[^0-9]", "");
			if (startTime.length() == 3) {
				startTime = "0" + startTime;
			} // 3桁補正

			// MySQLのTIME型が最も喜ぶ「HH:mm:00」形式に完全固定
			if (startTime.length() == 4) {
				startTime = startTime.substring(0, 2) + ":" + startTime.substring(2, 4) + ":00";
			}
		}
		return startTime;
	}

	/**
	 * 画面から送信された退勤時間をデータベース登録用の形式に変換します。
	 * <p>
	 * 退勤時間は休みの場合空白になっており、データベースへは "00:00:00" で登録を行います。<br>
	 * 出勤した日に関しては、出勤時間同様に分単位で送られてくる時間にコロンと秒（:00）を加え、秒単位まで表示する形式に整形します。
	 * </p>
	 *
	 * @param request   画面から送信された「出勤日」「新しい出退勤時刻」「備考」を含むDTO
	 * @param startTime 出勤時間をDBへ登録する形式へ整えた時間
	 * @return MySQLのTIME型に対応した「HH:mm:00」形式の文字列
	 */
	private String endTimeFixed(CreateWorkRequest request, String startTime) {
		String endTime = request.getEndTime();
		// 💡【修正】出勤時間が「休み」の場合、または退勤自体に「休み」が紛れ込んだ場合も安全に00:00:00で同期する
		if (endTime.isEmpty()) {
			endTime = "00:00:00";
		} else {
			// 数字だけにクレンジング (例: "2500" や "25:00")
			endTime = endTime.replaceAll("[^0-9]", "");
			if (endTime.length() == 3) {
				endTime = "0" + endTime;
			} // 3桁補正

			// 「時:分:00」にして秒数を常に00で固定
			if (endTime.length() == 4) {
				endTime = endTime.substring(0, 2) + ":" + endTime.substring(2, 4) + ":00";
			}
		}
		return endTime;
	}

	/**
	 * 画面にて確認ボタンが押下された際、同日の勤務データが既に登録されているかチェックを行います。
	 * <p>
	 * 対象日にデータが存在しない場合は新たにINSERTでデータを登録し、既に存在する場合はUPDATEで情報を上書き（永続化）します。
	 * </p>
	 *
	 * @param shainId   ログインを行っている社員の登録ID
	 * @param workDay   登録対象として指定された日付
	 * @param startTime 整形済みの出勤時間(HH:mm:ss)
	 * @param endTime   整形済みの退勤時間(HH:mm:ss)
	 * @param note      備考欄（有給申請などの情報）
	 */
	private void upsertAttendance(int shainId, LocalDate workDay, String startTime, String endTime, String note) {
		int count = shainDataMapper.countAttendanceData(shainId, workDay);

		//データの重複状況に応じて、INSERTまたはUPDATEを適切に分岐実行します。
		if (count > 0) {
			shainDataMapper.updateAttendanceData(shainId, workDay, startTime, endTime, note);
		} else {
			shainDataMapper.insertAttendanceData(shainId, workDay, startTime, endTime, note);
		}
	}
}