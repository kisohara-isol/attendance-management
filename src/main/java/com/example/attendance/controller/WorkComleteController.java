package com.example.attendance.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.attendance.entity.ShainData;
import com.example.attendance.service.WorkComleteServiceImpl;

@Controller
public class WorkComleteController {

	@Autowired
	private WorkComleteServiceImpl comlete;

	/**
	 * ⭕ 役割1：完了画面（HTML）を表示する処理
	 */
	@GetMapping("/attendance/management/workcomlete")
	public String display(Model model, HttpSession session,
			@RequestParam("Year") int workYear,
			@RequestParam("Month") int workMonth,
			@RequestParam("attendanceDate") int attendanceDate,
			@RequestParam("salary") int salary) {

		ShainData shain = (ShainData) session.getAttribute("loginShain");

		// 画面（HTML）側で使えるようにModelへ格納する
		model.addAttribute("login_id", shain.getLoginId());
		model.addAttribute("yyyy", workYear);
		model.addAttribute("MM", workMonth);
		model.addAttribute("attendanceDate", attendanceDate);
		model.addAttribute("salary", salary);

		// 完了画面のHTMLテンプレートを返す
		return "attendance/management/workcomlete";
	}

	/**
	 * ⭕ 役割2：ダウンロードリンクが押されたときに、ファイルをダウンロードさせる処理
	 */
	@GetMapping("/attendance/management/workcomlete/download")
	public ResponseEntity<byte[]> download(HttpSession session,
			@RequestParam("Year") int workYear,
			@RequestParam("Month") int workMonth,
			@RequestParam("attendanceDate") int attendanceDate,
			@RequestParam("salary") int salary) {

		ShainData shain = (ShainData) session.getAttribute("loginShain");

		// 1. Serviceを呼び出して、固定長データのバイト配列をもらう
		byte[] fileBytes = comlete.createFile(shain, workYear, workMonth, attendanceDate, salary);

		// 2. ブラウザへのダウンロードへの出力設定
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		// ファイル名を設定
		String fileName = shain.getLoginId() + "_" + workYear + String.format("%02d", workMonth) + "_salary.csv";
		headers.setContentDispositionFormData("attachment", fileName);

		// 3. 画面遷移はせず、ファイルデータそのものを応答（Response）として返却する
		return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
	}
}