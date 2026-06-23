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

@Controller
public class WorkSubmissionController {
	@Autowired
	private WorkSubmissionService submissionService;

	/**
	 * worktableから指定された年・月を受け取る
	 * 指定された月の平日（祝日を除く）の日数を算出
	 * 受け取った情報でDBから情報を受け取る
	 * 実際働いた日数を算出（有給は備考欄から算出）
	 * 社員情報から給与の情報を取り出し計算
	 * 時間ごとに条件を付け総額を算出 
	 * */
	// WorkSubmissionController 側
	@GetMapping("/attendance/management/worksubmission")
	public String display(Model model, HttpSession session,
			@ModelAttribute("WorkSubmissionRequest") WorkSubmissionRequest submission,
			@RequestParam("Year") int workYear,
			@RequestParam("Month") int workMonth) {

		// セッションからユーザー情報を取得
		ShainData shain = (ShainData) session.getAttribute("loginShain");

		// セッション切れチェック
		if (session == null || shain == null) {
			LogUtil.warn("W99999");
			return "redirect:/attendance/management/login";
		}
		model.addAttribute("shain", shain);

		/**
		 * 年と月から平日の日数を算出
		 * 実際働いた日数を算出
		 * */
		submissionService.dateCounts(workMonth, workYear, submission, shain);

		model.addAttribute("mustDay", submission.getMustDay());
		model.addAttribute("attendanceDay", submission.getAttendanceDay());
		model.addAttribute("paidHoliDay", submission.getPaidHoliDay());
		model.addAttribute("salary", submission.getSalary());

		// ⭕ Controller側の修正例
		// (直前の行で int result = workSubmissionService.dateCounts(month, year, submission, shain); を実行しているはずです)

		// ★ 計算結果が入ったオブジェクト（submission）を、HTMLに合せて "attendanceInfo" という名前で登録する！
		model.addAttribute("attendanceInfo", submission);

		return "attendance/management/worksubmission";
	}

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

	@GetMapping("/attendance/management/back_table")
	public String Confirmed() {
		return "redirect:/attendance/management/worktable";

	}
}