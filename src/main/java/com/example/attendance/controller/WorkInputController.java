package com.example.attendance.controller;

import java.time.LocalDate;

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
import com.example.attendance.dto.validator.ValidGroupOrder;
import com.example.attendance.util.ControllerUtil;
import com.example.attendance.util.DateTimeUtil;
import com.example.attendance.util.LogUtil;

/**
 * /attemdance/management/workinputのコントローラクラス
 * @author kato
 * @version 2.0 2026-06-23 kato
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
	public String display(
			@ModelAttribute("workDay") LocalDate workDay,
			@ModelAttribute("startTime") String startTime,
			@ModelAttribute("endTime") String endTime, //受け取り自体はLocalTimeでも可能
			@ModelAttribute("note") String note,
			@ModelAttribute CreateWorkRequest request, Model model, HttpSession session,
			RedirectAttributes redirect) {
		//sessionの確認
		if (!ControllerUtil.isKeepingSession(session, "loginShain")) {
			LogUtil.warn("W99999");

			redirect.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");

			return "redirect:/attendance/management/login"; // 最初のページへ
		}

		LogUtil.info("workDay= {},startTime={},endTime={},note={}", workDay, startTime, endTime, note);
		//ログイン社員をmodelに詰める
		model.addAttribute("loginShain", session.getAttribute("loginShain"));

		if (request.getStartTime() == null) {
			//CreateWorkRequestのstartTime(必須項目)が空なら、持ってきた各要素を詰め込む
			request.setWorkDay(workDay);
			//時間要素は、コロン区切りの場合それを取り外す
			request.setStartTime(DateTimeUtil.nonColonStyle(startTime).orElse(startTime));
			request.setEndTime(DateTimeUtil.nonColonStyle(endTime).orElse(endTime));
			request.setNote(note);
		}

		//ログ出力
		LogUtil.info("[{}]:Display \"/attendance/management/workinput\", session=[{}]",
				WorkInputController.class.getSimpleName(), session.getAttribute("loginShain"));
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
	public String redilect(@ModelAttribute @Validated(ValidGroupOrder.class) CreateWorkRequest request,
			BindingResult result, Model model, RedirectAttributes redirect) {
		//バリデーションチェック
		if (result.hasErrors()) {
			ControllerUtil.warnAllBindErrors(result, CreateWorkRequest.getAnnotationCodeMap());
			return "attendance/management/workinput"; //相対パスにしないとエラーとなると報告有り
		}

		redirect.addFlashAttribute("createWorkRequest", request);
		//ログを出すかは検討
		return "redirect:/attendance/management/workconfirm";
	}

}
