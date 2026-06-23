package com.example.attendance.service;

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

	private final Map<String, Integer> errorMap = new HashMap<>();

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

		// 💡【最重要：ガードロジック】
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
			errorMap.clear();
			return 1;
		} else {
			// パスワード間違い（失敗カウントを1増やす）
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

	@Override
	public ShainData getShainById(String loginId) {
		return shainDataMapper.selectShainById(loginId);
	}
}