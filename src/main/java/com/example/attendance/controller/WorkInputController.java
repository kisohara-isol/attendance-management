package com.example.attendance.controller;

import static java.util.Map.*;

import java.util.Collections;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.attendance.dto.CreateWorkRequest;
import com.example.attendance.util.LogUtil;

/**
 * /attemdance/management/workinputのコントローラクラス
 * @author kato
 */
@Controller
public class WorkInputController {

	/**
	 * dtoクラス"CreateWorkRequest"に存在するフィールドの列挙子。<br>
	 * 各列挙子は、自身の表すフィールドが持つアノテーションを、{アノテーション名,エラーコード}のMapで保持する
	 */
	private enum CreateWorkRequestFileds {
		/**出勤日(workDay)*/
		WORK_DAY(entry("typeMismatch", "W30003"), entry("NotNull", "W30001")),
		/**出勤時間(startTime)*/
		START_TIME(entry("NotEmpty", "W30002"), entry("Pattern", "W30004")),
		/**退勤時間(endTIme)*/
		END_TIME(entry("typeMismatch", "W30006"), entry("HolidayCheck", "W30005")),
		/**備考(note)*/
		NOTE();

		/**列挙子が持つ、{アノテーション名,エラーコード}のmap*/
		private Map<String, String> annotations;

		/**コンストラクタ
		 * @param entries 各フィールドのアノテーションとエラーコード
		 */
		private CreateWorkRequestFileds(Map.Entry<String, String>... entries) {
			this.annotations = Map.ofEntries(entries);
		}

		/**
		 * 引数で指定されたアノテーション名に対応するエラーコードを返す。
		 * @param annotationType アノテーション名
		 * @return エラーコード。対応するアノテーションが存在しない場合はnull
		 */
		private String getErrorCode(String annotationType) {
			if (this.annotations.size() == 0) {
				return null;
			}
			return this.annotations.get(annotationType);
		}
	}

	/**
	 * /attendance/management/workinputの描写メソッド
	 * @param request 画面の入力を保持するdto
	 * @param model モデル
	 * @param session セッション
	 * @param redirect リダイレクト
	 * @return /attendance/management/workinputの描写
	 */
	@GetMapping(value = "/attendance/management/workinput")
	public String display(@ModelAttribute CreateWorkRequest request, Model model, HttpSession session,
			RedirectAttributes redirect) {
		//sessionの確認
		boolean isKeepingSession = Collections.list(session.getAttributeNames()) //sessionに保存されている中身の名前をlistに
				.stream().anyMatch(x -> "loginShain".equals(x)); //streamで"loginShain"の存在を確認
		if (!isKeepingSession) {
			LogUtil.warn("W99999");

			redirect.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");

			return "redirect:/attendance/management/login"; // 最初のページへ
		}

		//ログ出力
		LogUtil.info("[{}]:Display \"/attendance/management/workinput\", session=[{}]",
				WorkInputController.class.getSimpleName(), session.getAttribute("loginShain"));

		//ログイン社員をmodelに詰める
		model.addAttribute("loginShain", session.getAttribute("loginShain"));
		return "attendance/management/workinput";
	}

	/**
	 * バリデーションチェックを行い、エラーがなければ/attendance/management/workconfirmへリダイレクトする
	 * @param request 入力内容を保持するdto。バリデーションの対象
	 * @param result バリデーションの結果
	 * @param model
	 * @param redirect
	 * @return /attendance/management/workconfirmへのリダイレクト
	 */
	@PostMapping("/attendance/management/workinput")
	public String redilect(@ModelAttribute @Validated CreateWorkRequest request, BindingResult result, Model model,
			RedirectAttributes redirect) {
		//バリデーションチェック
		if (result.hasErrors()) {
			result.getFieldErrors().stream().forEach(x -> {
				//フィールド名に対応する列挙子を取得
				CreateWorkRequestFileds feild = switch (x.getField()) {
				case "workDay" -> CreateWorkRequestFileds.WORK_DAY;
				case "startTime" -> CreateWorkRequestFileds.START_TIME;
				case "endTime" -> CreateWorkRequestFileds.END_TIME;
				case "note" -> CreateWorkRequestFileds.NOTE;
				default -> throw new IllegalArgumentException("Unexpected value: " + x.getField());
				};
				//アノテーションを取得
				String annotationType = x.getCode();
				LogUtil.warn(feild.getErrorCode(annotationType));
			});
			return "attendance/management/workinput"; //相対パスにしないとエラーとなると報告有り
		}

		redirect.addFlashAttribute("createWorkRequest", request);
		//ログを出すかは検討
		return "redirect:/attendance/management/workconfirm";
	}

}
