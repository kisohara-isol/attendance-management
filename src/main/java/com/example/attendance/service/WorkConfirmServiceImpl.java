package com.example.attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.dto.CreateWorkRequest;
import com.example.attendance.repository.AttendanceTableMapper;
import com.example.attendance.util.DateTimeUtil;

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
	private AttendanceTableMapper attendanceTableMapper;

	/**
	 * 勤務日と社員IDからDBを検索し、該当する勤務が存在するかを判別します。
	 * @param attendanceDate 勤務日
	 * @param shainId 社員ID
	 * @return 該当する勤務が有ればtrue
	 */
	@Override
	public boolean isExistAttendanceData(LocalDate attendanceDate, int shainId) throws DataAccessException {
		var result = attendanceTableMapper.selectAttendanceByWorkDay(shainId, attendanceDate);
		return result.isPresent();
	}

	/**
	 * 入力データを加工してデータベースを更新します。
	 * <p>
	 * <b>【処理フロー】</b>
	 * <ol>
	 * <li>画面から届いた出勤時間（文字列）を検証し、未入力や「休み」の場合は {@code 00:00} を、それ以外は {@link LocalTime} に変換します。</li>
	 * <li>画面から届いた退勤時間（文字列）を検証し、未入力の場合は {@code 00:00:00} を、それ以外は末尾に{@code :00}を追記します。</li>
	 * <li>整合性を整えたデータを {@link attendanceTableMapper#updateAttendanceData} に渡し、永続化します。</li>
	 * </ol>
	 * </p>
	 *
	 * @param request 勤務登録画面から送信された入力値（日付、出退勤時間、備考など）が格納されたDTO
	 * @param shainId 登録する勤務記録の持ち主である社員の社員ID
	 * 
	 */
	@Override
	@Transactional
	public void updateAttendanceData(CreateWorkRequest request, int shainId) {
		//出勤時間と退勤時間を"hh:MM:ss"表記のString型に変更
		retouchStartTimeAndEndTime(request);
		// 要素を取得
		LocalDate workDay = request.getWorkDay();
		LocalTime startTime = LocalTime.parse(request.getStartTime());
		String endTime = request.getEndTime();
		String note = request.getNote();

		// マッパーを呼び出してデータベースに登録
		attendanceTableMapper.updateAttendanceData(shainId, workDay, startTime, endTime, note);

	}

	/**
	 * 入力データを加工してデータベースに登録します。
	 * <p>
	 * <b>【処理フロー】</b>
	 * <ol>
	 * <li>画面から届いた出勤時間（文字列）を検証し、未入力や「休み」の場合は {@code 00:00} を、それ以外は {@link LocalTime} に変換します。</li>
	 * <li>画面から届いた退勤時間（文字列）を検証し、未入力の場合は {@code 00:00:00} を、それ以外は末尾に{@code :00}を追記します。</li>
	 * <li>整合性を整えたデータを {@link attendanceTableMapper#insertAttendanceData} に渡し、永続化します。</li>
	 * </ol>
	 * </p>
	 *
	 * @param request 勤務登録画面から送信された入力値（日付、出退勤時間、備考など）が格納されたDTO
	 * @param shainId 登録する勤務記録の持ち主である社員の社員ID
	 * 
	 */
	@Override
	@Transactional // ⭕ データベースへのインサート処理を伴うため、トランザクション管理を行います
	public void insertAttendanceData(CreateWorkRequest request, int shainId) throws DataAccessException {
		//出勤時間と退勤時間を"hh:MM:ss"表記のString型に変更
		retouchStartTimeAndEndTime(request);
		// 要素を取得
		LocalDate workDay = request.getWorkDay();
		LocalTime startTime = LocalTime.parse(request.getStartTime());
		String endTime = request.getEndTime();
		String note = request.getNote();

		// マッパーを呼び出してデータベースに登録
		attendanceTableMapper.insertAttendanceData(shainId, workDay, startTime, endTime, note);

	}

	/**
	 * CreateWorkRequestを受け取り、{@code startTime}及び{@code endTime}の値を
	 * {@code hh:MM:ss}形式の文字列に書き換えます。
	 * <p>それぞれの入力値が{@code hhMM}の形式を満たしていない場合
	 * (入力値が「休み」やnullである場合も含む)、<br>代わりに{@code 00:00:00}が
	 * 設定されます。</p>
	 * @param request 修正対象のCreateWorkRequest
	 */
	private void retouchStartTimeAndEndTime(CreateWorkRequest request) {
		var startAndEnd = new HashMap<String, String>();
		startAndEnd.put("start", request.getStartTime());
		startAndEnd.put("end", request.getEndTime());

		startAndEnd.entrySet().stream()
				.forEach(entry -> {
					String retouched = DateTimeUtil.withColonStyle(entry.getValue()) //時間と分を:で区切る
							.orElseGet(() -> LocalTime.MIN.toString()) //値がない場合は00:00を取得
							+ ":00"; //秒要素を追加

					switch (entry.getKey()) {
					case "start" -> request.setStartTime(retouched);
					case "end" -> request.setEndTime(retouched);
					}
				});
	}
}