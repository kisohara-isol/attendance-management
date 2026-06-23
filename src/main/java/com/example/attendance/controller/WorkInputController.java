package com.example.attendance.controller;

import java.util.Collections;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.attendance.dto.CreateWorkRequest;
import com.example.attendance.util.CreateWorkRequestFields;
import com.example.attendance.util.LogUtil;

/**
 * /attemdance/management/workinputのコントローラクラス
 * @author kato
 */
@Controller
public class WorkInputController {

	/**
	 * /attendance/management/workinputの描写メソッド
	 * @param request 画面の入力を保持するdto
	 * @param model モデル
	 * @param session セッション
	 * @param redirect リダイレクト
	 * @return /attendance/management/workinputの描写
	 */
	/**
	 * /attendance/management/workinputの描写メソッド
	 */
	@GetMapping(value = "/attendance/management/workinput")
	public String display(
			Model model,
			HttpSession session,
			RedirectAttributes redirect,
			@RequestParam(value = "workYear", required = false) String year,
			@RequestParam(value = "workMonth", required = false) String month,
			@RequestParam(value = "selectedDay", required = false) String dayStr) { // selectedDay で安全に受け取る

		// セッションの確認
		boolean isKeepingSession = Collections.list(session.getAttributeNames())
				.stream().anyMatch(x -> "loginShain".equals(x));
		if (!isKeepingSession) {
			LogUtil.warn("W99999");
			redirect.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");
			return "redirect:/attendance/management/login";
		}
		CreateWorkRequest createWorkRequest = new CreateWorkRequest();

		// 安全に年・月・日を結合して LocalDate を組み立てる
		if (year != null && month != null && dayStr != null) {
			try {
				int y = Integer.parseInt(year);
				int m = Integer.parseInt(month);
				int d = Integer.parseInt(dayStr);

				// ここで安全に LocalDate 型をセット（400エラーの発生源を根本から遮断）
				createWorkRequest.setWorkDay(java.time.LocalDate.of(y, m, d));
			} catch (Exception e) {
				LogUtil.warn("日付の組み立てに失敗しました: {}-{}-{}", year, month, dayStr);
			}
		}

		model.addAttribute("loginShain", session.getAttribute("loginShain"));

		// ★ HTMLの th:object="${createWorkRequest}" に渡す
		model.addAttribute("createWorkRequest", createWorkRequest);

		return "attendance/management/workinput";
	}

	/**
	 * バリデーションチェックを行い、エラーがなければ/attendance/management/workconfirmへリダイレクトする
	 * @param request 入力内容を保持するdto。バリデーションの対象
	 * @param result バリデーションの結果
	 * @param model
	 * @param redirect
	 * @return /attendance/management/workconfirmへのリダイレクト
	 */
	@PostMapping("/attendance/management/workinput")
	public String redilect(@ModelAttribute @Validated CreateWorkRequest request, BindingResult result, Model model,
			RedirectAttributes redirect) {
		//バリデーションチェック
		if (result.hasErrors()) {
			result.getFieldErrors().stream().forEach(x -> {
				//フィールド名に対応する列挙子を取得
				CreateWorkRequestFields feild = switch (x.getField()) {
				case "workDay" -> CreateWorkRequestFields.WORK_DAY;
				case "startTime" -> CreateWorkRequestFields.START_TIME;
				case "endTime" -> CreateWorkRequestFields.END_TIME;
				case "note" -> CreateWorkRequestFields.NOTE;
				default -> throw new IllegalArgumentException("Unexpected value: " + x.getField());
				};
				//アノテーションを取得
				String annotationType = x.getCode();
				LogUtil.warn(feild.getErrorCode(annotationType));
			});
			return "attendance/management/workinput"; //相対パスにしないとエラーとなると報告有り
		}

		redirect.addFlashAttribute("createWorkRequest", request);
		//ログを出すかは検討
		return "redirect:/attendance/management/workconfirm";
	}

}
