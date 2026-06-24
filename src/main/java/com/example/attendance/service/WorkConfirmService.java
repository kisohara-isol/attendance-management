package com.example.attendance.service;

import java.time.LocalDate;

import org.springframework.dao.DataAccessException;

import com.example.attendance.dto.CreateWorkRequest;

/**
 * 勤務登録確認画面におけるビジネスロジックを定義するサービスインターフェース。
 * <p>
 * 画面からの入力値の加工（型変換や初期値の設定）および、
 * セッションからのログイン社員情報の取得を行い、データベースへの永続化処理を統括します。
 * @author Soeda
 * </p>
 */
public interface WorkConfirmService {
	boolean isExistAttendanceData(LocalDate attendanceDate, int shainId) throws DataAccessException;

	void insertAttendanceData(CreateWorkRequest createWorkRequest, int shainId) throws DataAccessException;
	
	void updateAttendanceData(CreateWorkRequest createWorkRequest, int shainId) throws DataAccessException;
	

}
