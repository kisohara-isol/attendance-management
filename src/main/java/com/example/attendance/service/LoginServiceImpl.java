package com.example.attendance.service;

import java.util.function.IntUnaryOperator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.entity.ShainData;
import com.example.attendance.repository.ShainDataMapper;

/**
 * /attendance/management/loginのサービスクラス
 */
@Service
public class LoginServiceImpl implements LoginService {

	/**
	 * DBアクセス用のマッパー
	 */
	@Autowired
	private ShainDataMapper shainDataMapper;

	/**
	 * 社員IDが渡された社員データと一致するDBのレコードを更新し、失敗回数を0にする。<br>
	 * その後、同じく失敗回数を0にリセットした社員データを返却する。
	 * @param shain 対象の社員データ
	 * @return 失敗回数(failureCount)が0になった社員データ
	 */
	@Override
	@Transactional
	public ShainData resetCountBothDbAndShainData(ShainData shain) {
		this.executeUpdateOneRecordById(shainDataMapper::resetFailureCountByShainId, shain.getShainId());
		//失敗カウントを0に
		shain.setFailureCount(0);
		return shain;
	}

	/**
	 * 社員IDが渡された社員データと一致するDBのレコードを更新し、失敗回数1増加する。
	 * @param shain 対象の社員データ
	 */
	@Override
	@Transactional
	public void incrementCount(ShainData shain) {
		this.executeUpdateOneRecordById(shainDataMapper::incrementFailureCountByShainId, shain.getShainId());
	}

	/**
	 * 社員IDを用いて1件のレコードのみを更新するメソッドを実行する。<br>
	 * 更新件数が1件でない場合はObjectOptimisticLockingFailureExceptionを発する、
	 * @param updateMethod 引数がshainIdであり、int型を返すDB更新メソッド
	 * @param shainId 社員ID
	 */
	@Transactional
	private void executeUpdateOneRecordById(IntUnaryOperator updateMethod, int shainId) {
		int updateResult = updateMethod.applyAsInt(shainId);
		if (updateResult != 1) {
			//更新が正常に行われなかった場合にエラーを投げる。
			//"ObjectOptimisticLockingFailureException":DataAccessExceptionのサブクラスで、「楽観的ロック」の失敗時に投げる
			throw new ObjectOptimisticLockingFailureException(ShainData.class, shainId);
		}
	}

	/**
	 *ログインIDから社員情報を取得する。
	 */
	@Override
	public ShainData getShainById(String loginId) {

		return shainDataMapper.selectShainById(loginId);

	}
}