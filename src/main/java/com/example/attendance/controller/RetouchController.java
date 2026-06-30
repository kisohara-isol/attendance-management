package com.example.attendance.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

import com.example.attendance.dto.RetouchRequest;
import com.example.attendance.entity.ShainData;
import com.example.attendance.service.RetouchService;
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

	private final DateTimeFormatter fmtime = DateTimeFormatter.ofPattern("HHmm");
	private final DateTimeFormatter fmdate = DateTimeFormatter.ofPattern("yyyy/MM/dd");
	private final DateTimeFormatter fmtimeColon = DateTimeFormatter.ofPattern("HH:mm[:ss]");

	@Autowired
	private RetouchService retouchService;

	/**
	 * 勤務修正画面の初期表示を行います。
	 *
	 * @param workDay        修正対象の勤務日
	 * @param startTime      修正前の出勤時間
	 * @param endTime        修正前の退勤時間
	 * @param note           修正前の備考
	 * @param retouchRequest 画面の入力フォームと連動するDTO
	 * @param model          画面へデータを送るためのModel
	 * @param session        セッションオブジェクト
	 * @return 勤務修正画面のHTMLパス、またはログイン画面へのリダイレクト
	 */
	@GetMapping(value = "/attendance/management/retouch")
	public String display(@RequestParam(value = "workDay") String workDay,
			@RequestParam(value = "startTime") String startTime, 
			@RequestParam(value = "endTime") String endTime,
			@RequestParam(value = "note") String note, 
			@ModelAttribute("retouchRequest") RetouchRequest retouchRequest, Model model,
			HttpSession session) {
		
		LogUtil.info("勤務修正画面に飛びました。");
		
		// セッション切れのチェック
		if (session == null || session.getAttribute("loginShain") == null) {
			LogUtil.warn("W99999");
			return "redirect:/attendance/management/login";
		}

		// 画面に修正前の時間・備考を表示させる（コロンを排除）
		retouchRequest.setStartTime(startTime != null ? startTime.replace(":", "") : "");
		retouchRequest.setEndTime(endTime != null ? endTime.replace(":", "") : "");
		retouchRequest.setNote(note);

		// 修正前のデータをModel経由でHTMLに渡す
		model.addAttribute("oldWorkDay", workDay);
		model.addAttribute("oldStartTime", startTime);
		model.addAttribute("oldEndTime", endTime);
		model.addAttribute("oldNote", note);

		return "attendance/management/retouch";
	}

	/**
	 * 画面から送信された新しい勤務情報で、出退勤データを上書き更新します。
	 *
	 * @param oldWorkDay    hiddenから引き継いだ修正前の勤務日
	 * @param oldStartTime  hiddenから引き継いだ修正前の出勤時間
	 * @param oldEndTime    hiddenから引き継いだ修正前の退勤時間
	 * @param oldNote       hiddenから引き継いだ修正前の備考
	 * @param request       新しく入力された値が格納されたDTO
	 * @param bindingResult 入力検証結果を保持するオブジェクト
	 * @param model         画面制御用Model
	 * @param session       セッションオブジェクト
	 * @return 成功時は勤務表画面へのリダイレクト、失敗時は自画面遷移
	 */
	@PostMapping(value = "/attendance/management/retouch")
	public String retouch(@RequestParam(value = "oldWorkDay") String oldWorkDay,
			@RequestParam(value = "oldStartTime") String oldStartTime,
			@RequestParam(value = "oldEndTime") String oldEndTime, 
			@RequestParam(value = "oldNote") String oldNote,
			@Validated @ModelAttribute("retouchRequest") RetouchRequest request, BindingResult bindingResult, Model model,
			HttpSession session) {
		
		LogUtil.info("入力処理を開始します。");

		// 💡【修正】セッションのnullチェックを最初に行い、ぬるぽクラッシュを防御
		if (session == null || session.getAttribute("loginShain") == null) {
			LogUtil.warn("W99999");
			return "redirect:/attendance/management/login";
		}
		ShainData shain = (ShainData) session.getAttribute("loginShain");

		// 単体バリデーション（@NotBlank）のエラー判定
		if (bindingResult.hasErrors()) {
			LogUtil.warn("W50001");
			model.addAttribute("oldWorkDay", oldWorkDay);
			model.addAttribute("oldStartTime", oldStartTime);
			model.addAttribute("oldEndTime", oldEndTime);
			model.addAttribute("oldNote", oldNote);
			return "attendance/management/retouch";
		}

		String newS = request.getStartTime();
		String newE = request.getEndTime();
		String newNote = request.getNote();

		LocalTime newStart = null;
		LocalTime newEnd = null;

		// 勤務形態による時間パース
		if ("休み".equals(newS)) {
			newStart = LocalTime.parse("0000", fmtime);
			newEnd = LocalTime.parse("0000", fmtime);
		} else {
			// 💡出勤時間のパース検証
			try {
				newStart = LocalTime.parse(newS, fmtime);
			} catch (DateTimeParseException e) {
				bindingResult.rejectValue("startTime", "error.startTime", "休みでない場合はHHmm形式で入力してください。");
				LogUtil.warn("W50002");
			}
			
			// 💡退勤時間のパース検証
			try {
				newEnd = LocalTime.parse(newE, fmtime);
			} catch (DateTimeParseException e) {
				bindingResult.rejectValue("endTime", "error.endTime", "HHmm形式で入力してください。");
				LogUtil.warn("W50003");
			}

			// パースエラーが1つでもあれば画面に差し戻す
			if (bindingResult.hasErrors()) {
				model.addAttribute("oldWorkDay", oldWorkDay);
				model.addAttribute("oldStartTime", oldStartTime);
				model.addAttribute("oldEndTime", oldEndTime);
				model.addAttribute("oldNote", oldNote);
				return "attendance/management/retouch";
			}
		}

		// 修正前データのパース
		LocalDate workDay = LocalDate.parse(oldWorkDay, fmdate);
		LocalTime start = LocalTime.parse(oldStartTime, fmtimeColon);
		LocalTime end = LocalTime.parse(oldEndTime, fmtimeColon);

		// 主処理（サービス・永続層呼び出し）
		try {
			retouchService.retouchAttendance(newStart, newEnd, newNote, shain.getShainId(), workDay, start, end, oldNote);
		} catch (DataAccessException e) {
			LogUtil.error("E10001");
			model.addAttribute("oldWorkDay", oldWorkDay);
			model.addAttribute("oldStartTime", oldStartTime);
			model.addAttribute("oldEndTime", oldEndTime);
			model.addAttribute("oldNote", oldNote);
			return "attendance/management/retouch";
		}

		return "redirect:/attendance/management/worktable";
	}
}