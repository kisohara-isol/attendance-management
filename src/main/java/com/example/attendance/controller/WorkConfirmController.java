package com.example.attendance.controller;

import java.time.LocalDate;
import java.time.LocalTime;
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
 * 勤務登録確認画面に関する画面遷移やリクエストを制御するコントローラークラス。
 * <p>
 * 入力画面から送信された勤務情報を確認画面に表示する処理（GET）と、
 * 確認画面で登録が確定された際にサービス層へ処理を委譲する処理（POST）を提供します。
 * </p>
 * * @author Soeda
 */
@Controller
public class WorkConfirmController {

	/**
	 * 勤務登録に関するビジネスロジックを処理するサービス
	 */
	@Autowired
	private WorkConfirmService workConfirmService;

	/**
	 * 日付をスラッシュ区切りの文字列（例: "2026/06/02"）に変換するためのフォーマッター
	 */
	private static final DateTimeFormatter SLASH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

	/**
	 * 勤務登録の確認画面を表示（ディスプレィ）します。
	 * <p>
	 * ユーザーが見やすいよう、日付データにはスラッシュ（{@code /}）を、
	 * 4桁の時刻データにはコロン（{@code :}）をそれぞれ挿入して画面（Model）に設定します。
	 * </p>
	 *
	 * @param createWorkRequest 前画面から引き継いだ入力データが格納されたDTO
	 * @param session           HTTPセッションオブジェクト
	 * @param model             画面（Thymeleafテンプレート）にデータを渡すためのModelオブジェクト
	 * @return 勤務登録確認画面のテンプレートパス ({@code "attendance/management/workconfirm"})
	 */

	@GetMapping(value = "/attendance/management/workconfirm")
	public String display(@ModelAttribute CreateWorkRequest createWorkRequest, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) {

		/**sessionがnullの際のシュミレーション*/
		//session.setAttribute("loginShain", null);

		ShainData shain = (ShainData) session.getAttribute("loginShain");

		// 各コントローラーのセッション切れの処理部分
		if (session == null || shain == null) {
			LogUtil.warn("W99999");

			redirectAttributes.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");

			return "redirect:/attendance/management/login"; // 最初のページへ
		}
		// 1. 日付をスラッシュ区切り（yyyy/MM/dd）に変換してModelに格納
		LocalDate rawWorkDay = createWorkRequest.getWorkDay();
		if (rawWorkDay != null) {
			model.addAttribute("workDay", rawWorkDay.format(SLASH_FORMATTER));
		} else {
			model.addAttribute("workDay", "");
		}

		// 2. 出勤時間の真ん中にコロンを挟んでModelに格納（例: "0900" -> "09:00"）
		String startTime = createWorkRequest.getStartTime();
		if (startTime != null && startTime.length() == 4) {
			startTime = startTime.substring(0, 2) + ":" + startTime.substring(2, 4);
		}
		model.addAttribute("startTime", startTime);

		// 3. 退勤時間の真ん中にコロンを挟んでModelに格納（例: "1800" -> "18:00" / 未入力なら "00:00"）
		LocalTime endTime = createWorkRequest.getEndTime();

		model.addAttribute("endTime", endTime);

		// 4. 備考はそのままModelに格納
		model.addAttribute("note", createWorkRequest.getNote());

		model.addAttribute("createWorkRequest", createWorkRequest);

		return "attendance/management/workconfirm";
	}

	/**
	 * 確認画面で「追加」ボタンが押された際、データをデータベースに登録（インサート）します。
	 * <p>
	 * 実際のデータ加工や保存処理は {@link WorkConfirmService#insertAttendanceData} に委譲します。
	 * 登録完了後は、二重送信防止および画面更新のため、勤務表画面へリダイレクトします。
	 * </p>
	 *
	 * @param createWorkRequest 確認画面から送信された確定データが格納されたDTO
	 * @param model             Modelオブジェクト
	 * @param session           ログインユーザーの社員IDを取り出すためのHTTPセッションオブジェクト
	 * @return 勤務表画面へのリダイレクト指示 ({@code "redirect:/attendance/management/worktable"})
	 */
	@PostMapping("/attendance/management/workconfirm")
	public String input(@ModelAttribute CreateWorkRequest createWorkRequest, Model model, HttpSession session) {

		// サービス層を呼び出してDBへの登録を実行
		workConfirmService.insertAttendanceData(createWorkRequest, session);

		// ⭕ 登録処理（POST）の後は、ブラウザの「戻る」や「更新」による二重登録を防ぐため、
		// テンプレートパスを直接返すのではなく「redirect:」を使用するのが一般的なWeb開発のベストプラクティスです。
		return "redirect:/attendance/management/worktable";
	}
}
