package com.example.attendance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.attendance.repository.ShainDataMapper;

/**
 * /attendance/management/restoreのサービスクラス
 * @author kato
 */
@Service
public class RestoreServiceImpl implements RestoreService {
	/**DBの更新に用いるmapper*/
	@Autowired
	private ShainDataMapper mapper;

	/**
	 * 社員IDを受け取って、対象アカウントのロックを解除し、結果をbooleanで返す。
	 * <p>DB接続に障害が発生した場合はDataAccessException
	 * @param shainId 社員ID
	 * @return データ1件のみの更新に成功した場合true
	 */
	@Override
	public boolean executeRestoreShain(int shainId) {
		boolean result = mapper.resetStopFlugByShainId(shainId) == 1 ? true : false;
		return result;
	}
}