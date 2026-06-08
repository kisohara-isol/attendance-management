package com.example.attendance.service;

import com.example.attendance.entity.ShainData;

/**
 * ログイン認証に関するビジネスロジックを管理するサービスインターフェース。
 * * @author Soeda
 */
public interface LoginService {

	/**
	 * 入力されたログインIDとパスワードを元に、ログインの成否およびアカウント状態を判定します。
	 *
	 * @param loginId  画面から入力されたログインID
	 * @param password 画面から入力されたパスワード
	 * @return 判定結果のステータスと社員データを保持する {@link LoginResult} オブジェクト
	 */
	int loginJudge(String loginId, String password);

	int getRemainingAttempts(String loginId);
	
	ShainData getShainById(String loginId);
}