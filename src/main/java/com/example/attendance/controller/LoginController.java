package com.example.attendance.controller;

import java.util.Locale;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.attendance.dto.LoginRequest;
import com.example.attendance.entity.ShainData;
import com.example.attendance.service.LoginService;
import com.example.attendance.util.LogUtil;

/**
 * ログイン画面に関する画面表示および認証リクエストを制御するコントローラークラス。
 */
@Controller
public class LoginController {

	@Autowired
	private LoginService loginService;

	@Autowired
	private HttpSession session;

	@GetMapping(value = "/attendance/management/login")
	public String display(@ModelAttribute("loginRequest") LoginRequest loginRequest, Model model) {
		return "attendance/management/login";
	}

	@PostMapping("/attendance/management/login")
	public String login(@ModelAttribute("loginRequest") LoginRequest loginRequest, Model model, Locale locale,
			BindingResult bindingResult) {

		if (loginRequest.getLoginId() == null || loginRequest.getLoginId().trim().isEmpty()) {
			LogUtil.warn("W10001");
			model.addAttribute("errorMessage", "IDを入力してください");
			return "/attendance/management/login";
		}

		int statusCode = loginService.loginJudge(loginRequest.getLoginId(), loginRequest.getPassword());

		model.addAttribute("loginRequest", loginRequest);

		switch (statusCode) {
		case 1:
			ShainData loginShain = loginService.getShainById(loginRequest.getLoginId());
			session.setAttribute("loginShain", loginShain);
			return "redirect:/attendance/management/worktable";

		case 2:
			LogUtil.warn("W10005");
			model.addAttribute("errorMessage", "このアカウントは停止されています。管理者に問い合わせてください。");
			return "/attendance/management/login";

		case 3:
			LogUtil.warn("W10004");
			model.addAttribute("errorMessage", "ログイン失敗が3回に達したため、アカウントを停止しました。");
			return "/attendance/management/login";

		case 4:
			LogUtil.warn("W10006");
			model.addAttribute("errorMessage", "この社員IDは存在しません");
			return "/attendance/management/login";

		case 5:
			LogUtil.error("E10001");
			model.addAttribute("errorMessage", "DB接続時にエラーが発生しました。時間を空けて再度実行してください。");
			return "/attendance/management/login";

		case 0:
		default:
			// 💡【修正】セッションのnullデータの代わりに、画面から入力されたIDを渡して安全に残り回数を計算します
			int remaining = loginService.getRemainingAttempts(loginRequest.getLoginId());

			if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
				LogUtil.warn("W10002");
				model.addAttribute("errorMessage", "パスワードを入力してください。（残り: " + remaining + "回）");
			} else {
				LogUtil.warn("W10003");
				model.addAttribute("errorMessage", "ログインIDまたはパスワードが間違っています。（残り: " + remaining + "回）");
			}

			return "/attendance/management/login";
		}
	}
}