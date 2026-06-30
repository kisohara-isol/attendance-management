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

/**
 * 勤務登録・修正時の最終確認画面を制御するコントローラークラス。
 * <p>
 * 入力画面から引き継いだ勤務情報（出退勤時間・備考）を一時的に整形して確認画面に描画し、
 * 確定ボタンが押された際にサービス層を通じて永続化（DB登録・更新）を実行します。
 * </p>
 *
 * @author Hagiwara (or kato)
 */
@Controller
public class WorkConfirmController {

	/** 勤務データの登録・更新に関する永続化主処理を担うサービス */
	@Autowired
	private WorkConfirmService workConfirmService;

	/** 日付表示用のフォーマッター (YYYY/MM/DD) */
	private static final DateTimeFormatter SLASH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

	/**
	 * 勤務登録確認画面を表示します。
	 * <p>
	 * 入力された4桁の時間文字列（例: "0900"）を、画面表示用にコロン付き（"09:00:00"）へ整形してModelに格納します。
	 * </p>
	 *
	 * @param createWorkRequest  確認対象の入力データを保持するDTO
	 * @param session            HTTPセッションオブジェクト
	 * @param model              画面描画用Model
	 * @param redirectAttributes リダイレクト用フラッシュ属性保持オブジェクト
	 * @return 勤務確認画面のHTMLパス、またはログイン画面への強制リダイレクト
	 */
	@GetMapping(value = "/attendance/management/workconfirm")
	public String display(@ModelAttribute("createWorkRequest") CreateWorkRequest createWorkRequest, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) {

		// 💡【修正】セッションのnullチェックを最初に行い、ぬるぽクラッシュを防御
		if (session == null || session.getAttribute("loginShain") == null) {
			LogUtil.warn("W99999");
			redirectAttributes.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");
			return "redirect:/attendance/management/login";
		}
		ShainData shain = (ShainData) session.getAttribute("loginShain");

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

		// 2. 出勤時間のフォーマット（桁数ズレ・化け対策）
		String startTime = createWorkRequest.getStartTime();
		if (startTime != null && !startTime.isEmpty() && !"休み".equals(startTime)) {
			if (!startTime.contains(":") && startTime.length() == 4) {
				startTime = startTime.substring(0, 2) + ":" + startTime.substring(2, 4) + ":00";
			}
		}
		model.addAttribute("startTime", startTime);

		// 3. 退勤時間のフォーマット（桁数ズレ・化け対策）
		String endTime = createWorkRequest.getEndTime();
		if (endTime != null && !endTime.isEmpty()) {
			if (!endTime.contains(":") && endTime.length() == 4) {
				endTime = endTime.substring(0, 2) + ":" + endTime.substring(2, 4) + ":00";
			}
		}
		model.addAttribute("endTime", endTime);

		// 4. 備考をModelに格納
		model.addAttribute("note", createWorkRequest.getNote());
		model.addAttribute("createWorkRequest", createWorkRequest);

		return "attendance/management/workconfirm";
	}

	/**
	 * 確認画面で「確定」ボタンが押された際に、実際のデータベースへの登録・更新処理を依頼します。
	 *
	 * @param createWorkRequest  登録対象のデータを保持するDTO
	 * @param model              画面制御用Model
	 * @param session            HTTPセッションオブジェクト
	 * @param redirectAttributes 遷移先へ年月のクエリパラメータを引き継ぐためのオブジェクト
	 * @return 登録完了後の勤務表照会画面へのリダイレクト、またはエラー時の自画面遷移
	 */
	@PostMapping("/attendance/management/workconfirm")
	public String input(@ModelAttribute("createWorkRequest") CreateWorkRequest createWorkRequest, Model model, HttpSession session,
			RedirectAttributes redirectAttributes) {

		// 💡【修正】こちらもセッションのnullチェックを最初に行う形に補正
		if (session == null || session.getAttribute("loginShain") == null) {
			LogUtil.warn("W99999");
			redirectAttributes.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");
			return "redirect:/attendance/management/login";
		}
		ShainData shain = (ShainData) session.getAttribute("loginShain");

		try {
			// サービス層を呼び出してDBへの登録を実行
			workConfirmService.insertAttendanceData(createWorkRequest, shain);
		} catch (Exception e) {
			LogUtil.error("E10001", e);
			model.addAttribute("errorMessage", "DB接続時にエラーが発生しました。時間を空けて再度実行してください。");
			return "attendance/management/workconfirm";
		}

		// 登録後に一覧画面の該当年月へ安全に戻るためのパラメータ設定
		if (createWorkRequest.getWorkDay() != null) {
			String year = String.valueOf(createWorkRequest.getWorkDay().getYear());
			String month = String.valueOf(createWorkRequest.getWorkDay().getMonthValue());

			redirectAttributes.addAttribute("Year", year);
			redirectAttributes.addAttribute("Month", month);
		}

		return "redirect:/attendance/management/worktable";
	}
}