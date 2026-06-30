package com.example.attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.attendance.repository.RetouchMapper;

/**
 * 勤務修正に関するビジネスロジックを提供するサービス実装クラス。
 * <p>
 * {@link RetouchService} インターフェースを実装し、コントローラー層から渡された
 * 修正用パラメータを基に {@link RetouchMapper} を呼び出して、データベース（勤務表）の
 * 更新処理を行います。
 * </p>
 *
 * @author Hagiwara
 */
@Service
public class RetouchServiceImpl implements RetouchService {

	/** 勤務修正に関するデータベース操作を担うマッパーオブジェクト */
	@Autowired
	private RetouchMapper mapper;

	/**
	 * 出退勤テーブルのレコードを新しい入力値で更新します。
	 * <p>
	 * 指定された社員ID、日付、および変更前の出退勤時間・備考に一致する既存のレコードを特定し、
	 * 画面から入力された新しい出勤時間、退勤時間、備考で上書き更新を行います。
	 * 備考（newNote）が未入力（null）で渡された場合は、データ不整合を防止するため空文字に変換して登録します。
	 * </p>
	 *
	 * @param newStart 新しい出勤時間 (HH:mm)
	 * @param newEnd   新しい退勤時間 (HH:mm)
	 * @param newNote  新しい備考（変更理由など）
	 * @param shainId  対象の社員ID
	 * @param workday  対象の日付 (YYYY-MM-DD)
	 * @param start    変更前の出勤時間（レコード特定用のWHERE句に使用）
	 * @param end      変更前の退勤時間（レコード特定用のWHERE句に使用）
	 * @param memo     変更前の備考（レコード特定用のWHERE句に使用）
	 */
	@Override
	public void retouchAttendance(LocalTime newStart, LocalTime newEnd, String newNote, int shainId, LocalDate workday,
			LocalTime start, LocalTime end, String memo) {
		
		// 💡【安全化】新しい備考が null の場合は、空文字に変換してDBのクラッシュやデータ不整合を防ぐ
		String safeNewNote = (newNote == null) ? "" : newNote;
		
		// 💡変更前の備考（memo）も同様に null の可能性があれば空文字にして条件の一致率を上げる
		String safeMemo = (memo == null) ? "" : memo;

		// 安全な変数を渡してマッパーを呼び出し
		mapper.updateAttendanceTable(newStart, newEnd, safeNewNote, shainId, workday, start, end, safeMemo);
	}
}