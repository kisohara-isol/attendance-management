package com.example.attendance.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.attendance.dto.WorkTableRequest;
import com.example.attendance.entity.AttendanceData;
import com.example.attendance.entity.ShainData;
import com.example.attendance.service.WorkTableService;
import com.example.attendance.util.ControllerUtil;
import com.example.attendance.util.LogUtil;
import com.example.attendance.util.MessagesPropertiesUtil;

/**
 * 勤務表照会画面の表示およびデータ検索リクエストを制御するコントローラークラス。
 * <p>
 * ログインセッションのチェック、入力値のバリデーション、 およびサービス層を介した勤務表データの取得と画面へのマッピングを行います。
 * </p>
 * * @author Hagiawra
 */
@Controller
public class WorkTableController {

	/**サービスクラス*/
	@Autowired
	private WorkTableService workTableService;

	/**
	 * 勤務表照会ページの初期表示、および再表示を行います。
	 * <p>
	 * アクセス時にセッションチェックを行い、有効なセッションが存在しない場合はログイン画面へリダイレクトします。
	 * セッションが有効な場合は、ログイン社員情報を画面に反映して勤務表照会画面を表示します。
	 * </p>
	 *
	 * @param request 検索条件を保持するリクエストDTO
	 * @param model   画面へデータを渡すためのModelオブジェクト
	 * @param session ログイン状態を検証するためのHTTPセッション
	 * @return 遷移先のテンプレート名、またはログイン画面へのリダイレクトパス
	 * @version 2.0 2026-06-22 kato
	 */
	@GetMapping(value = "/attendance/management/worktable")
	public String display(@ModelAttribute WorkTableRequest request, Model model, HttpSession session) {

		LogUtil.info("勤務表照会ページに飛びました。");

		LocalDate ld = LocalDate.now();
		int year = ld.getYear();
		int month = ld.getMonthValue();

		request.setWorkYear(String.valueOf(year));
		request.setWorkMonth(String.valueOf(month));

		model.addAttribute("WorkTableRequest", request);

		// セッションからユーザー情報を取得・反映
		ShainData shain = (ShainData) session.getAttribute("loginShain");

		//		セッション切れの場合
		if (session == null || shain == null) {
			LogUtil.warn("W99999");
			return "redirect:/attendance/management/login";
		}

		// 反映させる
		model.addAttribute("shain", shain);

		if (model.containsAttribute("workList")
				&& model.getAttribute("workList") instanceof List existList) {
			if (existList.size() != 0) {
				//既に"workList"として空でないListが格納されているならそのまま表示(シチュエーションは不明)
				return "attendance/management/worktable";
			}
		}

		//表示するものがない場合は今月分の勤務表を表示
		var workList = workTableService.getAttendanceList(shain.getShainId(), year, month);
		LogUtil.debug("社員ID:{}の{}年{}月の勤務表を取得しました。", shain.getShainId(), year, month);
		model.addAttribute("workList", workList);

		return "attendance/management/worktable";

	}

	/**
	 * 指定された年月の勤務表データを検索し、照会画面へ反映します。
	 * <p>
	 * 入力された年月の未入力チェック、型チェック、および範囲チェック（月が1〜12であるか）を行い、
	 * すべてのチェックを通過した場合、ログイン社員のIDを基に勤務表リストを取得します。
	 * </p>
	 *
	 * @param request       入力された検索条件（年・月）を保持するリクエストDTO
	 * @param bindingResult 単項目チェック（相関チェック）の判定結果
	 * @param model         画面へデータを渡すためのModelオブジェクト
	 * @param session       ログイン社員情報を取得するためのHTTPセッション
	 * @return 自画面へのパス、またはセッション切れ時のログイン画面へのパス
	 */
	@PostMapping(value = "/attendance/management/worktable")
	public String table(@Validated @ModelAttribute("WorkTableRequest") WorkTableRequest request,
			BindingResult bindingResult, Model model, HttpSession session, RedirectAttributes redirectAttributes) {

		LogUtil.info("入力処理を開始します。");

		// セッションからユーザー情報を取得・反映
		ShainData shain = (ShainData) session.getAttribute("loginShain");
		//		セッション切れの場合
		if (session == null || shain == null) {
			LogUtil.warn("W99999");
			return "redirect:/attendance/management/login";
		}
		model.addAttribute("shain", shain);

		String workYear = request.getWorkYear();
		String workMonth = request.getWorkMonth();

		// 年月が空欄だった場合メッセージを表示する
		if (bindingResult.hasErrors()) {
			ControllerUtil.warnAllBindErrors(bindingResult, WorkTableRequest.getAnnotationCodeMap());
			return "attendance/management/worktable";
		}

		// 年の値が不正か調べる
		try {
			int year = Integer.parseInt(workYear);
			if(year < 2020) {
				//2020年以前の値ははじく
				bindingResult.rejectValue("workYear", "error.workYear", MessagesPropertiesUtil.getErrorMessage("W20005"));
				LogUtil.warn("W20005");
				return "attendance/management/worktable";
			}
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

		// 勤務表を取得・反映
		List<AttendanceData> workList = workTableService.getAttendanceList(shain.getShainId(),
				Integer.parseInt(workYear), Integer.parseInt(workMonth));

		model.addAttribute("workList", workList);
		LogUtil.debug("社員ID:{}の{}年{}月の勤務表を取得しました。", shain.getShainId(), workYear, workMonth);
		return "attendance/management/worktable";
	}

	/**
	 * 勤務表の各データに設けられた「変更」ボタンの押下時に、
	 * 該当行のデータを持ってworkinputへリダイレクトします。
	 * @param workDay 日付
	 * @param startTime 勤務開始時間
	 * @param endTime 勤務終了時間
	 * @param note 備考
	 * @param model モデル
	 * @param session セッション
	 * @param redirectAttributes flashAttributeの中に次ページに渡す情報が含まれる
	 * @return セッションが維持されていれば"/attendance/management/workinput"へのリダイレクト
	 */
	@PostMapping("/attendance/management/change")
	public String redirectChangeAtendance(
			@ModelAttribute("workDay") String workDay,
			@ModelAttribute("startTime") String startTime,
			@ModelAttribute("endTime") String endTime, //受け取り自体はLocalTimeでも可能?
			@ModelAttribute("note") String note,
			Model model, HttpSession session, RedirectAttributes redirectAttributes) {

		if (!ControllerUtil.isKeepingSession(session, "loginShain")) {
			LogUtil.warn("W99999");
			return "redirect:/attendance/management/login";
		}

		redirectAttributes
				.addFlashAttribute("workDay", workDay)
				.addFlashAttribute("startTime", startTime)
				.addFlashAttribute("endTime", endTime)
				.addFlashAttribute("note", note);

		LogUtil.info("勤務変更POST:{}", redirectAttributes.getFlashAttributes());
		return "redirect:/attendance/management/workinput";
	}

}
