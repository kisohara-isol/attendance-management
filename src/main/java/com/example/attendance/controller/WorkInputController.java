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
 * 勤務情報の新規登録における、初期入力画面の制御を行うコントローラークラス。
 *
 * @author kato
 */
@Controller
public class WorkInputController {

	/**
	 * 勤務新規入力画面を表示します。
	 *
	 * @param model    画面制御用Model
	 * @param session  HTTPセッション
	 * @param redirect リダイレクト用オブジェクト
	 * @param year     選択された年
	 * @param month    選択された月
	 * @param dayStr   選択された日
	 * @return 勤務入力画面のHTMLパス
	 */
	@GetMapping(value = "/attendance/management/workinput")
	public String display(
			Model model,
			HttpSession session,
			RedirectAttributes redirect,
			@RequestParam(value = "workYear", required = false) String year,
			@RequestParam(value = "workMonth", required = false) String month,
			@RequestParam(value = "selectedDay", required = false) String dayStr) {

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
				createWorkRequest.setWorkDay(java.time.LocalDate.of(y, m, d));
			} catch (Exception e) {
				LogUtil.warn("日付の組み立てに失敗しました: {}-{}-{}", year, month, dayStr);
			}
		}

		model.addAttribute("loginShain", session.getAttribute("loginShain"));
		model.addAttribute("createWorkRequest", createWorkRequest);

		return "attendance/management/workinput";
	}

	/**
	 * 新規入力された勤務データの単項目および相関チェックを行い、問題がなければ確認画面へリダイレクトします。
	 *
	 * @param request       画面から送信された入力データ
	 * @param result        検証結果オブジェクト
	 * @param model         画面制御用Model
	 * @param redirect      確認画面へフラッシュスコープ経由でデータを引き渡すためのオブジェクト
	 * @return 成功時は勤務確認画面へのリダイレクト、エラー時は自画面遷移
	 */
	@PostMapping("/attendance/management/workinput")
	public String redilect(@ModelAttribute("createWorkRequest") @Validated CreateWorkRequest request, BindingResult result, Model model,
			RedirectAttributes redirect) {
		
		// 💡【修正】@ModelAttribute("createWorkRequest") の名称をHTML側に明示的に合わせてバグを完全防止！
		if (result.hasErrors()) {
			result.getFieldErrors().stream().forEach(x -> {
				CreateWorkRequestFields feild = switch (x.getField()) {
				case "workDay" -> CreateWorkRequestFields.WORK_DAY;
				case "startTime" -> CreateWorkRequestFields.START_TIME;
				case "endTime" -> CreateWorkRequestFields.END_TIME;
				case "note" -> CreateWorkRequestFields.NOTE;
				default -> throw new IllegalArgumentException("Unexpected value: " + x.getField());
				};
				String annotationType = x.getCode();
				LogUtil.warn(feild.getErrorCode(annotationType));
			});
			return "attendance/management/workinput"; 
		}

		redirect.addFlashAttribute("createWorkRequest", request);
		return "redirect:/attendance/management/workconfirm";
	}
}