package com.example.attendance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.entity.ShainData;
import com.example.attendance.repository.ShainDataMapper;

@Service
public class LoginServiceImpl implements LoginService {

	@Autowired
	private ShainDataMapper shainDataMapper;

	/**
	 * ログイン時失敗した回数をアカウントごとに格納するmap
	 */
	// [変更]ログイン失敗回数はMYSQLに保存
//	private final Map<String, Integer> errorMap = new HashMap<>();

	/**
	 * ログインの判定と、失敗時のロック処理をすべて行う * @param loginId 画面から入力されたID
	 * 
	 * @param password 画面から入力されたパスワード
	 * @return 判定結果のステータスコード（0:失敗, 1:成功, 2:ロック中,
	 *         3:今回でロックされた,4:アカウントが存在しない,5:DB接続エラー）
	 */
	@Override
	@Transactional // DB更新を伴うため、Service層に付けるのが最適です
	public int loginJudge(String loginId, String password) {

		ShainData shain = null;

		// DBに接続できているか
		try {

			// 1. まず入力されたIDで社員が存在するか確認
			shain = getShainById(loginId);
			// shainDataMapper.selectShainDataがないため実際はIDとパスワードセットで検索は行えていない。

		} catch (DataAccessException e) {

			return 5; //
		}

		// 社員が存在しない場合は、認証失敗(4)を返す
		if (shain == null) {
			return 4;
		}

		// ----社員が存在する------
		// 2. すでにアカウントがロックされている（stopFlgが1）か確認
		if (shain.getStopFlg() == 1) {
			return 2; // すでにロック中
		}

		// 3. パスワードの照合
		if (shain.getPassword().equals(password)) {
			// ログイン成功

			return 1;
		} else {
			// パスワード間違い（ログイン失敗）

			// Mapから失敗回数を取得、一度も間違えていなければ0を設定
			// [変更]間違える度にfailure_countを一つ増やす
			int errorCounts = shain.getFailureCount();
			errorCounts++;
			shainDataMapper.updateFailureCount(errorCounts, shain.getShainId());

			// 3回連続で間違えたらアカウントをロックする
			// [変更] failureCountをMysqlに登録する
			// <br>Mysqlの方で3回以上の際停止フラグが立つようになっている。
			if (errorCounts >= 3) {
				if(loginId.equals("kisohara")) {
					return 0; // 管理者はロックされない
				}
				return 3; // 今回の失敗で新しくロックされた
			}

			return 0; // 通常のログイン失敗
		}
	}

	/**
	 * 残り試行回数を取得する（Controller側でメッセージに表示するため）
	 */
	@Override
	public int getRemainingAttempts(String loginId) {

		ShainData shain = getShainById(loginId);

		// Mapから失敗回数を取得、一度も間違えていなければ0を設定
		int errorCounts = shain.getFailureCount();
		return 3 - errorCounts;
	}

	/**
	 * ログインIDから社員情報を取得する。
	 */
	@Override
	public ShainData getShainById(String loginId) {

		return shainDataMapper.selectShainById(loginId);

	}
}