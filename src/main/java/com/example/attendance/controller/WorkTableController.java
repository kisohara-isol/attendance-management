package com.example.attendance.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.attendance.dto.WorkTableRequest;
import com.example.attendance.entity.AttendanceData;
import com.example.attendance.entity.ShainData;
import com.example.attendance.repository.WorkTableMapper;
import com.example.attendance.service.WorkTableService;
import com.example.attendance.util.LogUtil;

/**
 * 勤務表照会画面の表示およびデータ検索リクエストを制御するコントローラークラス。
 */
@Controller
public class WorkTableController {

	@Autowired
	private WorkTableService workTableService;
	@Autowired
	private WorkTableMapper workTableMapper;

	/**
	 * 勤務表照会ページの初期表示（当月）を行います。
	 */
	@GetMapping(value = "/attendance/management/worktable")
	public String display(Model model, HttpSession session, @ModelAttribute WorkTableRequest request,
			@RequestParam(value = "Year", required = false, defaultValue = "0") int workYear,
			@RequestParam(value = "Month", required = false, defaultValue = "0") int workMonth) {

		LogUtil.info("勤務表照会ページに飛びました。");

		// セッションからユーザー情報を取得
		ShainData shain = (ShainData) session.getAttribute("loginShain");

		// セッション切れチェック
		if (session == null || shain == null) {
			LogUtil.warn("W99999");
			return "redirect:/attendance/management/login";
		}
		model.addAttribute("shain", shain);

		int year = workYear;
		int month = workMonth;

		if (year == 0 || month == 0) {
			LocalDate ld = LocalDate.now();
			year = ld.getYear();
			month = ld.getMonthValue();
		}

		request.setWorkYear(String.valueOf(year));
		request.setWorkMonth(String.valueOf(month));
		model.addAttribute("WorkTableRequest", request);

		List<AttendanceData> calendarList = generateMonthlyCalendar(shain.getShainId(), year, month);
		model.addAttribute("workList", calendarList);

		return "attendance/management/worktable";
	}

	/**
	 * 指定された年月の勤務表データを検索し、カレンダー形式で表示します。
	 */
	@PostMapping(value = "/attendance/management/worktable")
	public String table(Model model, @Validated @ModelAttribute("WorkTableRequest") WorkTableRequest request,
			BindingResult bindingResult, HttpSession session, RedirectAttributes redirectAttributes) {

		LogUtil.info("入力処理を開始します。");

		ShainData shain = (ShainData) session.getAttribute("loginShain");
		if (session == null || shain == null) {
			LogUtil.warn("W99999");
			return "attendance/management/login";
		}
		model.addAttribute("shain", shain);

		String workYear = request.getWorkYear();
		String workMonth = request.getWorkMonth();

		// 単項目バリデーション（@Min, @Max, @NotBlank などDTO側のチェック）のエラーハンドリング
		if (bindingResult.hasErrors()) {
			String errorMessage = "";
			for (ObjectError oe : bindingResult.getAllErrors()) {
				errorMessage += oe.getDefaultMessage();
			}
			if (errorMessage.contains("2020～9999 までの年を入力してください。"))
				LogUtil.warn("W20001");
			if (errorMessage.contains("月を入力してください。"))
				LogUtil.warn("W20002");
			return "attendance/management/worktable";
		}

		int year;
		int month;

		// 1. 年のパースと範囲チェック
		try {
			year = Integer.parseInt(workYear);
		} catch (NumberFormatException e) {
			bindingResult.rejectValue("workYear", "error.workYear", "YYYY形式で入力してください。");
			LogUtil.warn("W20003");
			return "attendance/management/worktable";
		}

		// 💡【追加】2020年から9999年の範囲外だった場合のエラーハンドリング
		if (year < 2020 || 9999 < year) {
			bindingResult.rejectValue("workYear", "error.workYear", "2020～9999 までの年を入力してください。");
			LogUtil.warn("W20001"); // W20001の警告ログを出力
			return "attendance/management/worktable";
		}

		// 2. 月のパースと範囲チェック
		try {
			month = Integer.parseInt(workMonth);
		} catch (NumberFormatException e) {
			bindingResult.rejectValue("workMonth", "error.workMonth", "1から12の数字を入力してください。");
			LogUtil.warn("W20004");
			return "attendance/management/worktable";
		}

		if (month < 1 || 12 < month) {
			bindingResult.rejectValue("workMonth", "error.workMonth", "1から12の数字を入力してください。");
			LogUtil.warn("W20004");
			return "attendance/management/worktable";
		}

		List<AttendanceData> calendarList = generateMonthlyCalendar(shain.getShainId(), year, month);
		model.addAttribute("workList", calendarList);

		return "attendance/management/worktable";
	}

	/**
	 * 変更ボタンを押された時の処理。
	 */
	@PostMapping(value = "/attendance/management/worktable/to-input")
	public String input(Model model, @ModelAttribute("WorkTableRequest") WorkTableRequest request,
			HttpSession session, RedirectAttributes redirectAttributes) {

		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();
		String selectedDay = req.getParameter("selectedDay");

		redirectAttributes.addAttribute("workYear", request.getWorkYear());
		redirectAttributes.addAttribute("workMonth", request.getWorkMonth());
		redirectAttributes.addAttribute("selectedDay", selectedDay);

		return "redirect:/attendance/management/workinput";
	}

	/**
	 * 確定申請ボタンを押下で勤怠確定確認画面へ遷移
	 */
	@PostMapping(value = "/attendance/management/to_worksubmission")
	public String confirm(RedirectAttributes redirectAttributes,
			@ModelAttribute("WorkTableRequest") WorkTableRequest request, HttpSession session, Model model) {

		ShainData shain = (ShainData) session.getAttribute("loginShain");

		String year = request.getWorkYear();
		String month = request.getWorkMonth();

		int totalDaysInMonth = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month)).lengthOfMonth();

		int registeredDays = workTableMapper.countRegisteredDays(shain.getShainId(), year,
				String.format("%02d", Integer.parseInt(month)));

		if (Integer.parseInt(year) < 2020 || 9999 < Integer.parseInt(year)) {
			redirectAttributes.addFlashAttribute("errorMessage", "2020年～9999年以外の期間は申請できません。");
			LogUtil.warn("W20001"); // W20001の警告ログを出力
			return "redirect:/attendance/management/worktable";
		}
		if (registeredDays < totalDaysInMonth) {
			redirectAttributes.addFlashAttribute("errorMessage", "すべての日の勤務入力が完了していません。");
			redirectAttributes.addAttribute("Year", request.getWorkYear());
			redirectAttributes.addAttribute("Month", request.getWorkMonth());
			return "redirect:/attendance/management/worktable";
		}

		redirectAttributes.addAttribute("Year", request.getWorkYear());
		redirectAttributes.addAttribute("Month", request.getWorkMonth());

		return "redirect:/attendance/management/worksubmission";
	}

	/**
	 * 💡 カレンダーのマス目にデータを安全にマッピングする処理
	 */
	private List<AttendanceData> generateMonthlyCalendar(int shainId, int year, int month) {
		List<AttendanceData> dbWorkList = workTableService.getAttendanceList(shainId, year, month);
		List<AttendanceData> fullMonthList = new ArrayList<>();

		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate firstDay = yearMonth.atDay(1);
		LocalDate lastDay = yearMonth.atEndOfMonth();

		for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
			int dayNum = date.getDayOfMonth();
			String dayOfWeekKanji = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPANESE);

			AttendanceData targetData = null;
			for (AttendanceData dbData : dbWorkList) {
				if (dbData.getWorkDay() != null) {
					String cleanDayStr = dbData.getWorkDay().replaceAll("[^0-9]", "");

					if (cleanDayStr.length() >= 2) {
						int dbDay = Integer.parseInt(cleanDayStr.substring(cleanDayStr.length() - 2));
						if (dbDay == dayNum) {
							targetData = dbData;
							break;
						}
					} else if (!cleanDayStr.isEmpty()) {
						if (Integer.parseInt(cleanDayStr) == dayNum) {
							targetData = dbData;
							break;
						}
					}
				}
			}

			if (targetData != null) {
				targetData.setWorkDay(String.valueOf(dayNum));
				targetData.setDayOfWeek(dayOfWeekKanji);
				fullMonthList.add(targetData);
			} else {
				AttendanceData emptyData = new AttendanceData();
				emptyData.setWorkDay(String.valueOf(dayNum));
				emptyData.setDayOfWeek(dayOfWeekKanji);
				emptyData.setStartTime("");
				emptyData.setEndTime("");
				emptyData.setOverTime("");
				emptyData.setNote("");
				emptyData.setBreakDay(false);
				fullMonthList.add(emptyData);
			}
		}

		return fullMonthList;
	}
}