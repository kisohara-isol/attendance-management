package com.example.attendance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.example.attendance.controller.RestoreController;
import com.example.attendance.repository.ShainDataMapper;

/**
 * ロック状態の社員アカウントを復旧（有効化）するためのビジネスロジックを提供するサービス実装クラス。
 *
 * @author kato
 */
@Service
public class RestoreServiceImpl implements RestoreService {

	/** 社員データの更新に用いるマッパーオブジェクト */
	@Autowired
	private ShainDataMapper mapper;

	/**
	 * ログインIDを基に、対象アカウントのロックを強制解除します。
	 * <p>
	 * このメソッドは管理者画面からのみ呼び出されます。
	 * 内部で {@link ShainDataMapper#resetStopFlugByShainId(int)} を実行します。
	 * </p>
	 *
	 * @param shainId 解除対象の社員ID
	 * @return 復旧に成功した場合は true
	 * @throws DataAccessException DB接続に失敗した場合
	 * @see RestoreController
	 * @since 2026/06/25
	 */
	@Override
	public boolean executeRestoreShain(int shainId) {
		// 更新成功数が 1 件であるかを判定
		boolean result = mapper.resetStopFlugByShainId(shainId) == 1;
		return result;
	}
}