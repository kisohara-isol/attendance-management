package com.example.attendance.service;

/**
 * @author Soeda
 * */

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.entity.ShainData;
import com.example.attendance.repository.ShainDataMapper;
import com.example.attendance.util.LogUtil;

@Service
public class LoginServiceImpl implements LoginService {

	@Autowired
	private ShainDataMapper shainDataMapper;

	/**
	 * ログイン時失敗した回数をアカウントごとに格納するmap
	 * */
	private final Map<String, Integer> errorMap = new HashMap<>();

	/**
	 * ログインの判定と、失敗時のロック処理をすべて行う
	 * * @param loginId  画面から入力されたID
	 * @param password 画面から入力されたパスワード
	 * @return 判定結果のステータスコード（0:失敗, 1:成功, 2:ロック中, 3:今回でロックされた,4:アカウントが存在しない,5:DB接続エラー）
	 */
	@Override
	@Transactional // DB更新を伴うため、Service層に付けるのが最適です
	public int loginJudge(String loginId, String password) {

		ShainData shain = null;

		//DBに接続できているか
		try {

			// 1. まず入力されたIDで社員が存在するか確認
			shain = getShainById(loginId);

		} catch (DataAccessException e) {

			return 5; // 
		}

		// 社員が存在しない場合は、認証失敗(4)を返す
		if (shain == null) {
			return 4;
		}

		// 2. すでにアカウントがロックされている（stopFlgが1）か確認
		if (shain.getStopFlg() == 1) {
			return 2; // すでにロック中
		}

		// 3. パスワードの照合
		if (shain.getPassword().equals(password)) {
			// ログイン成功
			errorMap.clear();//ログイン成功時errorMapを完全にリセット

			return 1;
		} else {
			// パスワード間違い（ログイン失敗）

			//Mapから失敗回数を取得、一度も間違えていなければ0を設定
			int errorCounts = errorMap.getOrDefault(loginId, 0) + 1;
			errorMap.put(loginId, errorCounts);

			// 3回連続で間違えたらアカウントをロックする
			if (errorCounts >= 3) {
				shain.setStopFlg(1);
				try {
					shainDataMapper.updateShainData(shain);
				} catch (DataAccessException e) {
					LogUtil.error("E10001");
					return 5;
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
		//Mapから失敗回数を取得、一度も間違えていなければ0を設定
		int errorCounts = errorMap.getOrDefault(loginId, 0);
		return 3 - errorCounts;
	}

	
	/**
	 *ログインIDから社員情報を取得する。
	 */
	@Override
	public ShainData getShainById(String loginId) {

		return shainDataMapper.selectShainById(loginId);

	}
}