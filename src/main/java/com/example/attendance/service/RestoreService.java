package com.example.attendance.service;

/**
 * ロック（停止）状態のアカウントを復旧するためのビジネスロジックを定義するサービスインターフェース。
 *
 * @author kato
 */
public interface RestoreService {
	
	/**
	 * 指定された社員IDに対応するアカウントの停止フラグ（stop_flg）およびログイン失敗回数を安全にリセットし、有効化します。
	 *
	 * @param shainId ロックを解除する対象の社員ID
	 * @return データベースの更新が正常に1件行われ、復旧に成功した場合は {@code true}、対象が存在しない等の場合は {@code false}
	 */
	boolean executeRestoreShain(int shainId);
}