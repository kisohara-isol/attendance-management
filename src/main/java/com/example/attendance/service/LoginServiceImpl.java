package com.example.attendance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.entity.ShainData;
import com.example.attendance.repository.ShainDataMapper;
import com.example.attendance.util.LogUtil;

/**
 * ログイン認証処理およびアカウントロック制御に関するビジネスロジックを提供するサービス実装クラス。
 * * @author kato
 */
@Service
public class LoginServiceImpl implements LoginService {

	/** 社員データへのアクセスを担当するマッパーオブジェクト */
	@Autowired
	private ShainDataMapper shainDataMapper;

	/**
	 * ログインIDとパスワードを基に、ログインの成否およびアカウントロックの判定を行います。
	 * <p>
	 * このメソッドはトランザクション管理されており、処理中に例外が発生した場合はDB操作がロールバックされます。
	 * 失敗カウントが3回に達したアカウントは自動的に停止状態（stopFlg = 1）となり、
	 * それ以降は失敗カウント（failureCount）がそれ以上増えないようガードされます。
	 * </p>
	 *
	 * @param loginId  ログイン画面から入力された社員のログインID
	 * @param password ログイン画面から入力されたパスワード
	 * @return 判定結果を表すステータスコード
	 * <ul>
	 * <li>1: ログイン成功（カウントおよびロックフラグは0にリセットされます）</li>
	 * <li>2: すでにアカウント停止（ロック）中</li>
	 * <li>3: 今回の失敗により、累計3回となって新しくアカウントが停止（ロック）された</li>
	 * <li>4: 該当するログインID（アカウント）が存在しない</li>
	 * <li>5: データベースアクセスエラーなどのシステム例外</li>
	 * <li>0: 通常のログイン失敗（パスワード誤り、1回目または2回目）</li>
	 * </ul>
	 */
	@Override
	@Transactional
	public int loginJudge(String loginId, String password) {

		ShainData shain = null;

		try {
			shain = getShainById(loginId);
		} catch (DataAccessException e) {
			return 5;
		}

		if (shain == null) {
			return 4; // アカウントが存在しない
		}

		// すでにフラグが立っている（stopFlg == 1）ときは、これ以上 failureCount を増やさないよう即座に終了する
		if (shain.getStopFlg() == 1) {
			return 2; // すでにロック中（failure_count は 3 のまま固定されます）
		}

		// パスワードの照合
		if (shain.getPassword().equals(password)) {
			// ログイン成功：失敗カウントとロック状態をクリーンにリセット
			shain.setFailureCount(0);
			shain.setStopFlg(0);
			shainDataMapper.updateFailureCount(shain);
			shainDataMapper.updateShainData(shain); // 必要に応じてstopFlgも安全のためリセット
			return 1;

		} else {
			// パスワード間違い（失敗カウントを1増やす）
			// ⚠️【注意】kisohara アカウントのみロックを回避する特殊制御
			if(loginId.equals("kisohara")) {
				return 0;
			}
			int errorCounts = shain.getFailureCount() + 1;
			shain.setFailureCount(errorCounts);
			shainDataMapper.updateFailureCount(shain);

			// 3回になったら stopFlg を 1 にする
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

			return 0; // 通常のログイン失敗（1回目、2回目）
		}
	}

	/**
	 * 対象アカウントがロックされるまでの残りログイン可能回数を算出します。
	 * <p>
	 * 規定回数である3回から、現在の失敗回数（failureCount）を引いた数を返します。
	 * 対象アカウントが存在しない場合は、デフォルト値として3を返します。
	 * </p>
	 *
	 * @param loginId 残り回数を調査する対象のログインID
	 * @return ロックまでの残り回数（最低値は0）
	 */
	@Override
	public int getRemainingAttempts(String loginId) {
		ShainData shain = getShainById(loginId);
		if (shain == null) {
			return 3;
		}
		int errorCounts = shain.getFailureCount();
		int remaining = 3 - errorCounts;
		return remaining < 0 ? 0 : remaining;
	}

	/**
	 * 指定されたログインIDを基に、データベースから社員データを1件取得します。
	 *
	 * @param loginId 検索対象のログインID
	 * @return 該当する社員データオブジェクト、存在しない場合はnull
	 */
	@Override
	public ShainData getShainById(String loginId) {
		return shainDataMapper.selectShainById(loginId);
	}
}