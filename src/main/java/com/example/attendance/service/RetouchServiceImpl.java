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
	@Autowired
	private RetouchMapper mapper;

	/**
	 * 出退勤テーブルのレコードを更新します。
	 * <p>
	 * 指定された社員ID、日付、および変更前の出退勤時間・備考に一致するレコードを特定し、 新しい出勤時間、退勤時間、備考で上書きします。
	 * </p>
	 *
	 * @param newStart 新しい出勤時間 (HH:mm)
	 * @param newEnd   新しい退勤時間 (HH:mm)
	 * @param newNote  新しい備考（変更理由など）
	 * @param shainId  対象の社員ID
	 * @param workday  対象の日付 (YYYY-MM-DD)
	 * @param start    変更前の出勤時間（レコード特定用）
	 * @param end      変更前の退勤時間（レコード特定用）
	 * @param memo     変更前の備考（レコード特定用）
	 */
	@Override
	public void retouchAttendance(LocalTime newStart, String newEnd, String newNote, int shainId, LocalDate workday,
			LocalTime start, String end, String memo) {
		mapper.updateAttendanceTable(newStart, newEnd, newNote, shainId, workday, start, end, memo);
	}
	
	/**
	 * 勤務データを登録します。
	 * <p>引数で指定された社員ID、勤務日、始業・終業時間、および備考を元に、
	 * 新しい勤務記録をシステムに永続化（追加）します。</p>
	 *
	 * @param shainId   勤務データを登録する対象の社員ID
	 * @param workDay   勤務対象の日付 ({@link LocalDate})
	 * @param startTime 始業時刻 ({@link LocalTime})
	 * @param endTime   終業時刻（退勤前の状態や特定の入力形式に対応する文字列型）
	 * @param note      勤務に関する備考・特記事項（業務内容や遅刻・早退理由など）
	 */
	@Override
	public void addAttendance(int shainId, LocalDate workDay, LocalTime startTime, String endTime, String note) {
		mapper.insertAttendanceTable(shainId, workDay, startTime, endTime, note);
	}
}
