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
	@GetMapping(value = "/attendance/management/workinput")
	public String display(@ModelAttribute CreateWorkRequest request, Model model, HttpSession session,
			RedirectAttributes redirect) {
		//sessionの確認
		boolean isKeepingSession = Collections.list(session.getAttributeNames()) //sessionに保存されている中身の名前をlistに
				.stream().anyMatch(x -> "loginShain".equals(x)); //streamで"loginShain"の存在を確認
		if (!isKeepingSession) {
			LogUtil.warn("W99999");

			redirect.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");

			return "redirect:/attendance/management/login"; // 最初のページへ
		}

		//ログ出力
		LogUtil.info("[{}]:Display \"/attendance/management/workinput\", session=[{}]",
				WorkInputController.class.getSimpleName(), session.getAttribute("loginShain"));

		//ログイン社員をmodelに詰める
		model.addAttribute("loginShain", session.getAttribute("loginShain"));
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
