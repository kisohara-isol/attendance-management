package com.example.attendance.controller;

import java.time.LocalDate;
import java.util.List;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.attendance.dto.WorkTableRequest;
import com.example.attendance.entity.AttendanceData;
import com.example.attendance.entity.ShainData;
import com.example.attendance.service.WorkTableService;
import com.example.attendance.util.LogUtil;

/**
 * 勤務表照会画面の表示およびデータ検索リクエストを制御するコントローラークラス。
 * <p>
 * ログインセッションのチェック、入力値のバリデーション、 およびサービス層を介した勤務表データの取得と画面へのマッピングを行います。
 * </p>
 * * @author Hagiawra
 */
@Controller
public class WorkTableController {

	@Autowired
	private WorkTableService workTableService;

	/**
	 * 勤務表照会ページの初期表示、および再表示を行います。
	 * <p>
	 * アクセス時にセッションチェックを行い、有効なセッションが存在しない場合はログイン画面へリダイレクトします。
	 * セッションが有効な場合は、ログイン社員情報を画面に反映して勤務表照会画面を表示します。
	 * </p>
	 *
	 * @param model   画面へデータを渡すためのModelオブジェクト
	 * @param session ログイン状態を検証するためのHTTPセッション
	 * @param request 検索条件を保持するリクエストDTO
	 * @return 遷移先のテンプレート名、またはログイン画面へのリダイレクトパス
	 */
	@GetMapping(value = "/attendance/management/worktable")
	public String display(Model model, HttpSession session, @ModelAttribute WorkTableRequest workTableRequest) {

		LogUtil.info("勤務表照会ページに飛びました。");

		LocalDate ld = LocalDate.now();
		int year = ld.getYear();
		int month = ld.getMonthValue();

		workTableRequest.setWorkYear(String.valueOf(year));
		workTableRequest.setWorkMonth(String.valueOf(month));

		model.addAttribute("WorkTableRequest", workTableRequest);

		model.addAttribute("year", year);
		model.addAttribute("month", month);

		// セッションからユーザー情報を取得・反映
		ShainData shain = (ShainData) session.getAttribute("loginShain");

//		セッション切れの場合
		if (session == null || shain == null) {
			LogUtil.warn("W99999");
			return "redirect:/attendance/management/login";
		} else {
			// 反映させる
			model.addAttribute("shain", shain);
			return "attendance/management/worktable";
		}
	}

	/**
	 * 指定された年月の勤務表データを検索し、照会画面へ反映します。
	 * <p>
	 * 入力された年月の未入力チェック、型チェック、および範囲チェック（月が1〜12であるか）を行い、
	 * すべてのチェックを通過した場合、ログイン社員のIDを基に勤務表リストを取得します。
	 * </p>
	 *
	 * @param model         画面へデータを渡すためのModelオブジェクト
	 * @param request       入力された検索条件（年・月）を保持するリクエストDTO
	 * @param bindingResult 単項目チェック（相関チェック）の判定結果
	 * @param session       ログイン社員情報を取得するためのHTTPセッション
	 * @return 自画面へのパス、またはセッション切れ時のログイン画面へのパス
	 */
	@PostMapping(value = "/attendance/management/worktable")
	public String table(Model model, @Validated @ModelAttribute("WorkTableRequest") WorkTableRequest request,
			BindingResult bindingResult, HttpSession session, RedirectAttributes redirectAttributes) {

		LogUtil.info("入力処理を開始します。");

		// セッションからユーザー情報を取得・反映
		ShainData shain = (ShainData) session.getAttribute("loginShain");
//		セッション切れの場合
		if (session == null || shain == null) {
			LogUtil.warn("W99999");
			return "attendance/management/login";
		}
		model.addAttribute("shain", shain);

		String workYear = request.getWorkYear();
		String workMonth = request.getWorkMonth();

		// 年月が空欄だった場合メッセージを表示する
		if (bindingResult.hasErrors()) {
			// 入力値が不正だった理由を取得
			String errorMessage = "";
			for (ObjectError oe : bindingResult.getAllErrors()) {
				errorMessage += oe.getDefaultMessage();
			}
			// ログに流す
			if (errorMessage.contains("年を入力してください。")) {
				LogUtil.warn("W20001");
			}
			if (errorMessage.contains("月を入力してください。")) {
				LogUtil.warn("W20002");
			}
			return "attendance/management/worktable";
		}

		// 年の値が不正か調べる
		try {
			int year = Integer.parseInt(workYear);
		} catch (NumberFormatException e) {
			bindingResult.rejectValue("workYear", "error.workYear", "YYYY形式で入力してください。");
			LogUtil.warn("W20003");
			return "attendance/management/worktable";
		}
		// 月の値が不正か調べる(数字以外ならエラー)
		try {
			int month = Integer.parseInt(workMonth);
		} catch (NumberFormatException e) {
			bindingResult.rejectValue("workMonth", "error.workMonth", "1から12の数字を入力してください。");
			LogUtil.warn("W20004");
			return "attendance/management/worktable";
		}
		// 月の値が不正か調べる(1～12の数字)
		if (Integer.parseInt(workMonth) < 1 || 12 < Integer.parseInt(workMonth)) {
			bindingResult.rejectValue("workMonth", "error.workMonth", "1から12の数字を入力してください。");
			LogUtil.warn("W20004");
			return "attendance/management/worktable";
		}

		int year = Integer.parseInt(workYear);
		int month = Integer.parseInt(workMonth);

//		// DBから勤務表を取得・反映
		List<AttendanceData> workList = workTableService.getCalendar(shain.getShainId(), year, month);

		model.addAttribute("workList", workList);
		
		model.addAttribute("year", year);
		model.addAttribute("month", month);

		return "attendance/management/worktable";
	}

	@PostMapping(value = "/attendance/management/workchange")
	public String change(Model model, @RequestParam("workDay") String workDay,
			@RequestParam(value = "startTime") String startTime,
			@RequestParam(value = "endTime") String endTime, @RequestParam(value = "note") String note,
			@RequestParam(value = "isRegistration") boolean isRegistration, HttpSession session, RedirectAttributes redirectAttributes) {

		// セッションからユーザー情報を取得・反映
		ShainData shain = (ShainData) session.getAttribute("loginShain");
//				セッション切れの場合
		if (session == null || shain == null) {
			LogUtil.warn("W99999");
			return "attendance/management/login";
		}
		model.addAttribute("shain", shain);

		redirectAttributes.addFlashAttribute("oldWorkDay", workDay);
		redirectAttributes.addFlashAttribute("oldStartTime", startTime);
		redirectAttributes.addFlashAttribute("oldEndTime", endTime);
		redirectAttributes.addFlashAttribute("oldNote", note);
		redirectAttributes.addFlashAttribute("isRegistration", isRegistration);

		return "redirect:/attendance/management/retouch";
	}

	@PostMapping(value = "/attendance/management/worksubmit")
	public String submitWorkTable(
			Model model, HttpSession session, RedirectAttributes redirectAttributes,
			@RequestParam("year") int year, 
			@RequestParam("month") int month) {
		LogUtil.info("勤務表の一括提出処理を開始します。");
		
		// セッションからユーザー情報を取得・反映
		ShainData shain = (ShainData) session.getAttribute("loginShain");
//						セッション切れの場合
		if (session == null || shain == null) {
			LogUtil.warn("W99999");
			return "attendance/management/login";
		}
		model.addAttribute("shain", shain);

		// DBから勤務表を取得
		List<AttendanceData> submittedList = workTableService.getCalendar(shain.getShainId(), year, month);

		// 休みでないかつ勤務を提出していない日があったら提出できない
		for (AttendanceData ad : submittedList) {
			if(!ad.isBreakDay()) {
				if(!ad.isRegistration()) {
					LogUtil.warn("全ての勤怠を提出していません。");
					model.addAttribute("workList", submittedList);
					model.addAttribute("message", "全ての勤怠を提出していません。");
					
					// 入力欄に今の年月ではなく、勤務表を提出した年月をセットしておく
					WorkTableRequest tableRequest = new WorkTableRequest();
					
					tableRequest.setWorkYear(String.valueOf(year));
					tableRequest.setWorkMonth(String.valueOf(month));
					
					model.addAttribute("WorkTableRequest", tableRequest);
					
					return "attendance/management/worktable";
				}
			}
		}
		// 提出画面に送る
		redirectAttributes.addFlashAttribute("submittedList", submittedList);
		
		return "redirect:/attendance/management/worksubmission";
	}

}
