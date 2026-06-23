package com.example.attendance.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.attendance.entity.AttendanceData;
import com.example.attendance.entity.ShainData;
import com.example.attendance.service.WorkSubmissionService;
import com.example.attendance.util.LogUtil;

/**
 * 勤務表提出に関するリクエストを制御するコントローラークラスです。 *
 * <p>
 * 勤務表提出画面の表示処理、および勤務ファイル作成を含む提出完了処理を提供します。
 * </p>
 */
@Controller
public class WorkSubmissionController {

	/** 勤務表提出処理のビジネスロジックを提供するサービス */
	@Autowired
	WorkSubmissionService workSubmissionService;

	/**
	 * 勤務表提出確認画面を表示します。 *
	 * <p>
	 * 前画面から引き継いだ勤務データリスト（submittedList）を元に、総出勤日数、
	 * 実出勤日数、有給消化日数、総支給額、および対象年月を算出し、画面に渡します。
	 * </p>
	 * * @param submittedList 提出対象の勤務データリスト（前画面からFlash属性などで遷移することを想定）
	 * 
	 * @param model              画面へデータを渡すためのModelオブジェクト
	 * @param redirectAttributes リダイレクト時にデータを保持するためのオブジェクト
	 * @param session            ユーザー情報（社員データ）を取得するためのHTTPセッション
	 * @return 勤務表提出確認画面のテンプレートパス（セッション切れの場合はログイン画面へ遷移）
	 */
	@GetMapping(value = "/attendance/management/worksubmission")
	public String display(@ModelAttribute("submittedList") List<AttendanceData> submittedList, Model model,
			RedirectAttributes redirectAttributes, HttpSession session) {

		LogUtil.info("勤務表提出処理を開始します。");

		// セッション切れのチェック
		if (session == null || session.getAttribute("loginShain") == null) {
			LogUtil.warn("W99999");
			return "attendance/management/login";
		}
		// セッションからユーザー情報を取得・反映
		ShainData shain = (ShainData) session.getAttribute("loginShain");

		// 送られてきたリストから値を出す・モデルに反映
		int allWorkDays = workSubmissionService.getAllWorkDay(submittedList);
		model.addAttribute("allWorkDays", allWorkDays);
		int actualWorkDays = workSubmissionService.getActualWorkDay(submittedList);
		model.addAttribute("actualWorkDays", actualWorkDays);
		int paidHoliday = workSubmissionService.getPaidHoliday(submittedList);
		model.addAttribute("paidHoliday", paidHoliday);
		int allSalary = workSubmissionService.getAllSalary(shain.getShainId(), submittedList);
		model.addAttribute("allSalary", allSalary);

		// 年月を取得してhtmlに保存
		AttendanceData ad = submittedList.get(0);
		String[] dateParts = ad.getWorkDay().split("/");
		String yearMonth = String.format("%d%02d", Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]));
		model.addAttribute("yearMonth", yearMonth);
		return "attendance/management/worksubmission";
	}

	/**
	 * 勤務表提出の確定処理（完了処理）を行います。 *
	 * <p>
	 * 画面から送信された確定情報をもとに勤務ファイルを作成し、 完了画面（workcomplete）へリダイレクトします。
	 * </p>
	 * * @param yearMonth 対象年月 (例: "202606")
	 * 
	 * @param actualWorkDays     実出勤日数
	 * @param allSalary          総支給額
	 * @param model              画面へデータを渡すためのModelオブジェクト
	 * @param redirectAttributes 次の画面へファイル名を安全に引き渡すためのFlash属性コンテナ
	 * @param session            ユーザー情報（社員データ）を取得するためのHTTPセッション
	 * @return 提出完了画面へのリダイレクトパス（失敗時やセッション切れ時はそれぞれの適切な画面へ遷移）
	 */
	@PostMapping(value = "/attendance/management/worksubmission")
	public String complete(@RequestParam("yearMonth") String yearMonth,
			@RequestParam("actualWorkDays") int actualWorkDays, @RequestParam("allSalary") int allSalary, Model model,
			RedirectAttributes redirectAttributes, HttpSession session) {
		LogUtil.info("勤務表提出完了処理を開始します。");

		// セッション切れのチェック
		if (session == null || session.getAttribute("loginShain") == null) {
			LogUtil.warn("W99999");
			return "attendance/management/login";
		}
		// セッションからユーザー情報を取得・反映
		ShainData shain = (ShainData) session.getAttribute("loginShain");

		String fileName = workSubmissionService.createFile(yearMonth, actualWorkDays, allSalary, shain.getShainName(),
				shain.getLoginId());

		// ファイル作成に失敗した場合null
		if (fileName == null) {
			LogUtil.warn("ファイル作成に失敗しました。");
			return "redirect:/attendance/management/worksubmission";
		}

		redirectAttributes.addFlashAttribute("fileName", fileName); // ファイル名を表示させる用
		return "redirect:/attendance/management/workcomplete";
	}
}
