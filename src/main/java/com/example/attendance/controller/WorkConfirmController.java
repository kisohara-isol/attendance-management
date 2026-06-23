package com.example.attendance.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.attendance.dto.CreateWorkRequest;
import com.example.attendance.entity.ShainData;
import com.example.attendance.service.WorkConfirmService;
import com.example.attendance.util.LogUtil;

@Controller
public class WorkConfirmController {

	@Autowired
	private WorkConfirmService workConfirmService;

	private static final DateTimeFormatter SLASH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

	/**
	 * 勤務確認画面の表示
	 */
	@GetMapping(value = "/attendance/management/workconfirm")
	public String display(@ModelAttribute CreateWorkRequest createWorkRequest, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) {

		ShainData shain = (ShainData) session.getAttribute("loginShain");
		if (session == null || shain == null) {
			LogUtil.warn("W99999");
			redirectAttributes.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");
			return "redirect:/attendance/management/login";
		}

		// 1. 日付の処理
		LocalDate rawWorkDay = createWorkRequest.getWorkDay();
		if (rawWorkDay != null) {
			model.addAttribute("workDay", rawWorkDay.format(SLASH_FORMATTER));
			model.addAttribute("currentYear", String.valueOf(rawWorkDay.getYear()));
			model.addAttribute("currentMonth", String.valueOf(rawWorkDay.getMonthValue()));
		} else {
			model.addAttribute("workDay", "");
			model.addAttribute("currentYear", "");
			model.addAttribute("currentMonth", "");
		}

		// 2. 💡 【修正】出勤時間のフォーマット（桁数ズレ・化け対策）
		String startTime = createWorkRequest.getStartTime();
		if (startTime != null && !startTime.isEmpty() && !"休み".equals(startTime)) {
			// コロンが含まれておらず、4桁の数値形式の場合のみフォーマットする
			if (!startTime.contains(":") && startTime.length() == 4) {
				startTime = startTime.substring(0, 2) + ":" + startTime.substring(2, 4) + ":00";
			}
		}
		model.addAttribute("startTime", startTime);

		// 3. 💡 【修正】退勤時間のフォーマット（桁数ズレ・化け対策）
		// 退勤時間（例: "2900"）も出勤時間と同じルールで "29:00" に綺麗に変換します
		String endTime = createWorkRequest.getEndTime();
		if (endTime != null && !endTime.isEmpty()) {
			if (!endTime.contains(":") && endTime.length() == 4) {
				endTime = endTime.substring(0, 2) + ":" + endTime.substring(2, 4) + ":00";
			}
		}
		model.addAttribute("endTime", endTime);

		// 4. 備考はそのままModelに格納
		model.addAttribute("note", createWorkRequest.getNote());

		model.addAttribute("createWorkRequest", createWorkRequest);

		return "attendance/management/workconfirm";
	}

	/**
	 * 登録処理の実行
	 */
	@PostMapping("/attendance/management/workconfirm")
	public String input(@ModelAttribute CreateWorkRequest createWorkRequest, Model model, HttpSession session,
			RedirectAttributes redirectAttributes) {

		ShainData shain = (ShainData) session.getAttribute("loginShain");
		if (session == null || shain == null) {
			LogUtil.warn("W99999");
			redirectAttributes.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");
			return "redirect:/attendance/management/login";
		}

		try {
			// サービス層を呼び出してDBへの登録を実行
			workConfirmService.insertAttendanceData(createWorkRequest, shain);
		} catch (Exception e) {
			LogUtil.error("E10001");
			model.addAttribute("errorMessage", "DB接続時にエラーが発生しました。時間を空けて再度実行してください。");
			return "attendance/management/workconfirm";
		}

		// 💡 登録後に一覧へ戻る時も、HTMLのhiddenを経由せず、送られてきた日付から直接取得する（一番確実）
		if (createWorkRequest.getWorkDay() != null) {
			String year = String.valueOf(createWorkRequest.getWorkDay().getYear());
			String month = String.valueOf(createWorkRequest.getWorkDay().getMonthValue());

			redirectAttributes.addAttribute("Year", year);
			redirectAttributes.addAttribute("Month", month);
		}

		return "redirect:/attendance/management/worktable";
	}
}