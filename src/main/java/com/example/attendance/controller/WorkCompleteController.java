package com.example.attendance.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.attendance.entity.ShainData;
import com.example.attendance.util.LogUtil;

/**
 * 勤務表提出完了画面のリクエストを制御するコントローラークラスです。
 * <p>
 * 勤務表の提出処理が正常に終了した後の完了画面表示を制御します。
 * </p>
 */
@Controller
public class WorkCompleteController {

	/**
	 * 勤務表提出完了画面を表示します。 *
	 * <p>
	 * 前画面（提出処理）から引き継いだ作成ファイル名を画面に引き継ぐとともに、 セッションから社員情報を取得して画面に設定します。
	 * </p>
	 * * @param model 画面へデータを渡すためのModelオブジェクト
	 * 
	 * @param fileName 前画面からFlash属性などで引き継がれた、作成済みの勤務ファイル名
	 * @param session  ユーザー情報（社員データ）を取得するためのHTTPセッション
	 * @return 勤務表提出完了画面のテンプレートパス（セッション切れの場合はログイン画面へ遷移）
	 */
	@GetMapping(value = "/attendance/management/workcomplete")
	public String display(Model model, @ModelAttribute("fileName") String fileName, HttpSession session) {
		LogUtil.info("勤務表提出完了ページに移動しました。");

		// セッション切れのチェック
		if (session == null || session.getAttribute("loginShain") == null) {
			LogUtil.warn("W99999");
			return "attendance/management/login";
		}
		// セッションからユーザー情報を取得・反映
		ShainData shain = (ShainData) session.getAttribute("loginShain");
		model.addAttribute("shain", shain);
		return "/attendance/management/workcomplete";
	}
}
