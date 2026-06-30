package com.example.attendance.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.attendance.dto.WorkSubmissionRequest;
import com.example.attendance.entity.ShainData;
import com.example.attendance.service.WorkSubmissionService;
import com.example.attendance.util.LogUtil;

/**
 * 1ヶ月の勤務表の確定申請処理、および概算給与の集計・算出画面を制御するコントローラークラス。
 *
 * @author kato
 */
@Controller
public class WorkSubmissionController {
	
	@Autowired
	private WorkSubmissionService submissionService;

	/**
	 * 月別の勤務情報および給与概算の集計結果を含む、確定前確認画面を表示します。
	 *
	 * @param model      画面制御用Model
	 * @param session    HTTPセッション
	 * @param submission 算出結果を保持・バインドするDTO
	 * @param workYear   確定対象年
	 * @param workMonth  確定対象月
	 * @return 勤務申請確定画面のHTMLパス
	 */
	@GetMapping("/attendance/management/worksubmission")
	public String display(Model model, HttpSession session,
			@ModelAttribute("WorkSubmissionRequest") WorkSubmissionRequest submission,
			@RequestParam("Year") int workYear,
			@RequestParam("Month") int workMonth) {

		// 💡【修正】セッション自体のnullチェックを先頭に移動し、ぬるぽによる画面クラッシュを完全に防御！
		if (session == null || session.getAttribute("loginShain") == null) {
			LogUtil.warn("W99999");
			return "redirect:/attendance/management/login";
		}
		ShainData shain = (ShainData) session.getAttribute("loginShain");
		model.addAttribute("shain", shain);

		// サービス層で平日日数、実働日数、有給、深夜・休日手当を含む給与総額を一挙に集計・格納
		submissionService.dateCounts(workMonth, workYear, submission, shain);

		model.addAttribute("mustDay", submission.getMustDay());
		model.addAttribute("attendanceDay", submission.getAttendanceDay());
		model.addAttribute("paidHoliDay", submission.getPaidHoliDay());
		model.addAttribute("salary", submission.getSalary());

		// 成果物オブジェクトを Thymeleaf 側の変数名（"attendanceInfo"）にバインド
		model.addAttribute("attendanceInfo", submission);

		return "attendance/management/worksubmission";
	}

	/**
	 * 勤務データの確定処理を終了し、給与確定通知（完了）画面へパラメータを維持してリダイレクトします。
	 */
	@PostMapping(value = "/attendance/management/toworkcomlete")
	public String toWorkComlete(@RequestParam("Year") int workYear, @RequestParam("Month") int workMonth,
			@RequestParam("attendanceDate") int attendanceDate, @RequestParam("salary") int salary,
			RedirectAttributes redirectAttributes) {

		redirectAttributes.addAttribute("Year", workYear);
		redirectAttributes.addAttribute("Month", workMonth);
		redirectAttributes.addAttribute("attendanceDate", attendanceDate);
		redirectAttributes.addAttribute("salary", salary);

		return "redirect:/attendance/management/workcomlete";
	}

	/**
	 * 申請処理を中断し、元の勤務表照会画面に戻ります。
	 */
	@GetMapping("/attendance/management/back_table")
	public String Confirmed() {
		return "redirect:/attendance/management/worktable";
	}
}