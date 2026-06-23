package com.example.attendance.entity;

import lombok.Data;

/**
 * 社員データを管理するエンティティクラス。
 * <p>
 * データベースの {@x.code shain_data} テーブルのレコードに対応し、
 * 社員の基本情報やアカウントの状態を保持します。
 * @author Soeda
 * </p>
 */
@Data
public class ShainData {

	/**
	 * 社員ID（主キー）
	 */
	private int shainId;

	/**
	 * 社員名
	 */
	private String shainName;

	/**
	 * ログインID
	 */
	private String loginId;

	/**
	 * ログインパスワード
	 */
	private String password;

	/**
	 * アカウント停止フラグ
	 * <ul>
	 * <li>0: 通常（初期値）</li>
	 * <li>1: ログイン失敗3回などによるアカウントロック状態</li>
	 * </ul>
	 */
	private int stopFlg ;
	
	/**
	 * 失敗回数
	 * */
	private int failureCount;
}