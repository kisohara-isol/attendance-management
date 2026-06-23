package com.example.attendance.service;

import java.nio.charset.Charset;

import org.springframework.stereotype.Service;

import com.example.attendance.entity.ShainData;

@Service
public class WorkComleteServiceImpl implements WorkComleteService {

	/**
	 * 固定長ファイルのデータ（バイト配列）を作成する
	 */
	public byte[] createFile(ShainData shain, int year, int month, int workDate, long salary) {
		
		// 1. 各項目を仕様通りのバイト数に整形する
		String fixedName   = padRightSpaceBytes(shain.getShainName(), 20); // 0〜20バイト(20幅)
		String fixedDate   = String.format("%02d", workDate);              // 21〜22バイト(2幅)
		String fixedSalary = String.format("%10d", salary);               // 23〜32バイト(10幅)
		
		// 2. カンマは入れずに、そのまま隙間なく合体させる（最後に改行コードを入れる）
		String oneLine = fixedName + fixedDate + fixedSalary + "\r\n";
		
		// 3. 全体をMS932のバイト配列に変換してControllerへ返却する
		return oneLine.getBytes(Charset.forName("MS932"));
	}

	/**
	 * 文字列を指定の「バイト数」になるまで右側に半角スペースを詰めるメソッド
	 */
	private String padRightSpaceBytes(String target, int targetByteLength) {
		if (target == null) {
			target = "";
		}
		
		// 現在の文字列がMS932で何バイトあるか計算
		int currentByteLength = target.getBytes(Charset.forName("MS932")).length;
		
		// 足りないバイト数の分だけ、半角スペースを後ろに付け足す
		StringBuilder sb = new StringBuilder(target);
		while (currentByteLength < targetByteLength) {
			sb.append(" ");
			currentByteLength++;
		}
		
		return sb.toString();
	}
}