package com.example.attendance.service;

import com.example.attendance.entity.ShainData;

/**
 * ログイン認証に関するビジネスロジックを管理するサービスインターフェース。
 * * @author Soeda
 */
public interface LoginService {

	public ShainData resetCountBothDbAndShainData(ShainData shain);

	void incrementCount(ShainData shain);
	
	ShainData getShainById(String loginId);

}