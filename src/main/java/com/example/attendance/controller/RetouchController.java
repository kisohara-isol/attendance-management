package com.example.attendance.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.attendance.dto.RetouchRequest;
import com.example.attendance.dto.WorkTableRequest;
import com.example.attendance.entity.AttendanceData;
import com.example.attendance.entity.ShainData;
import com.example.attendance.service.RetouchService;
import com.example.attendance.service.WorkTableService;
import com.example.attendance.util.LogUtil;

/**
 * 勤務情報の修正リクエストを制御するコントローラークラス。
 * <p>
 * 勤務表照会画面から遷移し、特定の日の勤務実績(出勤時間・退勤時間・備考)を新しい入力値に書き換える処理を制御します。
 * 不正な入力値に対する型変換チェックや、データベース更新時の例外ハンドリングを含みます。
 * </p>
 *
 * @author Hagiwara
 */
@Controller
public class RetouchController {

	private DateTimeFormatter fmtime = DateTimeFormatter.ofPattern("HHmm");
	private DateTimeFormatter fmdate = DateTimeFormatter.ofPattern("yyyy/MM/dd");
	private DateTimeFormatter fmtimeColon = DateTimeFormatter.ofPattern("HH:mm[:ss]");

	@Autowired
	private RetouchService retouchService;

	@Autowired
	private WorkTableService workTableService;

	@GetMapping(value = "/attendance/management/retouch")
	public String display(RedirectAttributes redirectAttributes, @ModelAttribute RetouchRequest retouchRequest,
			Model model, HttpSession session) {

		LogUtil.info("勤務修正画面に飛びました。");

		// セッション切れの場合
		if (session == null || session.getAttribute("loginShain") == null) {
			LogUtil.warn("W99999");
			return "redirect:/attendance/management/login";
		}

		// 安全に値を取り出す（nullの可能性がある）
		Boolean isRegistrationObj = (Boolean) model.getAttribute("isRegistration");
		String oldStartTime = (String) model.getAttribute("oldStartTime");
		String oldEndTime = (String) model.getAttribute("oldEndTime");
		String oldNote = (String) model.getAttribute("oldNote");

		// null を完全に排除した安全なローカル変数を作る
		boolean isRegistration = (isRegistrationObj != null) ? isRegistrationObj : false;

		// 安全なローカル変数を使って条件分岐をする
		if (isRegistration) {
			// 画面に修正前の時間を表示させる（コロンを抜く処理）
			if (oldStartTime != null) {
				retouchRequest.setStartTime(oldStartTime.replace(":", ""));
			}
			if (oldEndTime != null) {
				retouchRequest.setEndTime(oldEndTime.replace(":", ""));
			}
		}

		// 備考をセット（nullチェック）
		if (oldNote != null) {
			retouchRequest.setNote(oldNote);
		}

		// HTMLの hidden（th:value）へ渡すデータを Model にしっかり詰め直す
		model.addAttribute("oldWorkDay", model.getAttribute("oldWorkDay"));
		model.addAttribute("oldStartTime", oldStartTime);
		model.addAttribute("oldEndTime", oldEndTime);
		model.addAttribute("oldNote", oldNote);
		model.addAttribute("isRegistration", isRegistration);

		return "attendance/management/retouch";
	}

	@PostMapping(value = "/attendance/management/retouch")
	public String retouch(@RequestParam(value = "oldWorkDay") String oldWorkDay, // HTMLのhiddenから受け取る
			@RequestParam(value = "oldStartTime") String oldStartTime,
			@RequestParam(value = "oldEndTime") String oldEndTime, @RequestParam(value = "oldNote") String oldNote,
			@RequestParam(value = "isRegistration") boolean isRegistration,
			@Validated @ModelAttribute RetouchRequest request, BindingResult bindingResult, Model model,
			HttpSession session, RedirectAttributes redirectAttribute) {

		LogUtil.info("入力処理を開始します。");

		// セッションからユーザー情報を取得・反映
		ShainData shain = (ShainData) session.getAttribute("loginShain");
		// セッション切れの場合
		if (session == null || shain == null) {
			LogUtil.warn("W99999");
			return "redirect:/attendance/management/login";
		}

		if (bindingResult.hasErrors()) {
			// 入力値が不正だった理由を取得
			LogUtil.warn("W50001");
			model.addAttribute("oldWorkDay", oldWorkDay);
			model.addAttribute("oldStartTime", oldStartTime);
			model.addAttribute("oldEndTime", oldEndTime);
			model.addAttribute("oldNote", oldNote);
			model.addAttribute("isRegistration", isRegistration);
			return "attendance/management/retouch";
		}

		// 入力された値を取得
		String newS = request.getStartTime();
		String newE = request.getEndTime();
		String newNote = request.getNote();

		LocalTime newStart = null;
		LocalTime newEnd = null;

		// 休みだった場合
		if ("休み".equals(newS)) {
			newStart = LocalTime.parse("0000", fmtime);
			newEnd = LocalTime.parse("0000", fmtime);
			newE = "0000";
		} else {
			// 時間が不正でないかtry-catchする 不正な値な場合エラーメッセージが表示される
			try {
				newStart = LocalTime.parse(newS, fmtime);
			} catch (DateTimeParseException e) {
				bindingResult.rejectValue("startTime", "error.startTime", "休みでない場合はHHmm形式で入力してください。");
				LogUtil.warn("W50002");
				model.addAttribute("oldWorkDay", oldWorkDay);
				model.addAttribute("oldStartTime", oldStartTime);
				model.addAttribute("oldEndTime", oldEndTime);
				model.addAttribute("oldNote", oldNote);
				model.addAttribute("isRegistration", isRegistration);
				return "attendance/management/retouch";
			}
			if (!newE.matches("^\\d{4}$")) {
				bindingResult.rejectValue("endTime", "休みでない場合はHHmm形式で入力してください。");
				LogUtil.warn("W50003");
				model.addAttribute("oldWorkDay", oldWorkDay);
				model.addAttribute("oldStartTime", oldStartTime);
				model.addAttribute("oldEndTime", oldEndTime);
				model.addAttribute("oldNote", oldNote);
				model.addAttribute("isRegistration", isRegistration);
				return "attendance/management/retouch";
			}
		}

		// htmlから修正前のデータを受け取る
		LocalDate workDay = LocalDate.parse(oldWorkDay, fmdate);

		LocalTime start = null;
		String end = null;
		newE = newE.substring(0, 2) + ":" + newE.substring(2);
		// 登録済みであれば書き換える
		if (isRegistration) {
			start = LocalTime.parse(oldStartTime);
			end = oldEndTime;
			try {
				retouchService.retouchAttendance(newStart, newE, newNote, shain.getShainId(), workDay, start, end,
						oldNote);
			} catch (DataAccessException e) {
				LogUtil.error("E10001");
				model.addAttribute("oldWorkDay", oldWorkDay);
				model.addAttribute("oldStartTime", oldStartTime);
				model.addAttribute("oldEndTime", oldEndTime);
				model.addAttribute("oldNote", oldNote);
				model.addAttribute("isRegistration", isRegistration);
				return "attendance/management/retouch";
			}
			// 未登録であれば新しく追加する
		} else {
			try {
				retouchService.addAttendance(shain.getShainId(), workDay, newStart, newE, newNote);
			} catch (DataAccessException e) {
				LogUtil.error("E10001");
				model.addAttribute("oldWorkDay", oldWorkDay);
				model.addAttribute("oldStartTime", oldStartTime);
				model.addAttribute("oldEndTime", oldEndTime);
				model.addAttribute("oldNote", oldNote);
				model.addAttribute("isRegistration", isRegistration);
				return "attendance/management/retouch";
			}
		}
		model.addAttribute("shain", shain);

		String[] dateParts = oldWorkDay.split("/");

		// 戻るとき元の画面を表示
		List<AttendanceData> workList = workTableService.getCalendar(shain.getShainId(), Integer.parseInt(dateParts[0]),
				Integer.parseInt(dateParts[1]));
		model.addAttribute("workList", workList);
		WorkTableRequest workTableRequest = new WorkTableRequest();
		workTableRequest.setWorkYear(String.valueOf(Integer.parseInt(dateParts[0])));
		workTableRequest.setWorkMonth(String.valueOf(Integer.parseInt(dateParts[1])));
		model.addAttribute("WorkTableRequest", workTableRequest);
		model.addAttribute("year", Integer.parseInt(dateParts[0]));
		model.addAttribute("month", Integer.parseInt(dateParts[1]));

		return "attendance/management/worktable";
	}
}
