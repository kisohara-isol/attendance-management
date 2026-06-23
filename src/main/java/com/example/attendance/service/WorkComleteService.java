package com.example.attendance.service;

import org.springframework.stereotype.Service;

import com.example.attendance.entity.ShainData;

@Service
public interface WorkComleteService {

	/**
	 * 固定長ファイルの作成
	 * ○○ファイルは固定長であり、ファイル名は「${login_id}_yyyymm_salary.csv」
	 * 0~20バイト：社員名
	 * 21~22バイト：出勤日数
	 * 23~32バイト：支給額
	 * @return 
	 * */
	byte[] createFile(ShainData shain, int year, int month, int workDate, long salary);

}
