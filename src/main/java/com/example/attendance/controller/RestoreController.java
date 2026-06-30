package com.example.attendance.controller;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.attendance.dto.RestoreRequest;
import com.example.attendance.service.RestoreService;
import com.example.attendance.util.LogUtil;

/**
 * ロック（停止）された社員アカウントの復旧処理に関する画面制御を行うコントローラークラス。
 * * @author kato
 */
@Controller
public class RestoreController {

	/** アカウント復旧のビジネスロジックを提供するサービスクラス */
	@Autowired
	private RestoreService restoreService;

	/**
	 * 社員復旧画面の初期表示を行います。
	 * <p>
	 * セッションをチェックし、管理者としてログイン状態であることを確認した上で画面を描画します。
	 * セッションが切れている場合はログイン画面へリダイレクトします。
	 * </p>
	 *
	 * @param restoreRequest 画面のフォームデータを格納するDTOオブジェクト
	 * @param session        管理者セッションの有無を確認するためのHTTPセッション
	 * @param redirect       リダイレクト先へフラッシュ属性を渡すためのオブジェクト
	 * @return 社員復旧画面のHTMLパス、またはログイン画面へのリダイレクト指示
	 */
	@GetMapping("/attendance/management/restore")
	public String display(@ModelAttribute("restoreRequest") RestoreRequest restoreRequest, HttpSession session,
			RedirectAttributes redirect) {

		// セッションの生存確認（loginShain の存在チェック）
		if (session == null || session.getAttribute("loginShain") == null) {
			LogUtil.warn("W99999");
			redirect.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");
			return "redirect:/attendance/management/login";
		}

		return "attendance/management/restore";
	}

	/**
	 * 画面から入力された社員IDを基に、該当アカウントのロック解除（復旧）処理を実行します。
	 * <p>
	 * バリデーションチェック、存在確認を行い、問題がなければアカウントを有効化して勤務表画面へリダイレクトします。
	 * 不備がある場合は同画面にエラーメッセージを表示します。
	 * </p>
	 *
	 * @param request        画面から送信された社員IDを含むDTOオブジェクト
	 * @param result         入力チェックの検証結果を保持するオブジェクト
	 * @param session        セッションの生存確認を行うためのHTTPセッション
	 * @param model          画面へエラーメッセージを渡すためのModelオブジェクト
	 * @param redirect       リダイレクト時の制御用オブジェクト
	 * @return 処理成功時は勤務表画面へのリダイレクト、失敗時は社員復旧画面のHTMLパス
	 */
	@PostMapping(value = "/attendance/management/restore")
	public String restoreAndRedirect(@ModelAttribute("restoreRequest") @Validated RestoreRequest request,
			BindingResult result,
			HttpSession session, Model model, RedirectAttributes redirect) {

		// セッションの再確認
		if (session == null || session.getAttribute("loginShain") == null) {
			LogUtil.warn("W99999");
			redirect.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");
			return "redirect:/attendance/management/login";
		}

		// 単体バリデーションチェック（@NotNull, @Positive）
		if (result.hasErrors()) {
			// 💡安全になった getErrorCode メソッドを呼び出して警告ログを出力
			result.getFieldErrors().forEach(x -> LogUtil.warn(RestoreRequest.getErrorCode(x.getCode())));
			return "attendance/management/restore";
		}

		// サービス層の復旧主処理を呼び出し
		try {
			if (!restoreService.executeRestoreShain(request.getShainId())) {
				// 対象の社員IDが存在しない、またはロックされていない場合
				model.addAttribute("errorMessage", "入力した社員IDに該当するロックされたアカウントは存在しません。");
				LogUtil.warn("W40003");
				return "attendance/management/restore";
			}
		} catch (DataAccessException e) {
			// データベース通信トラブル等の例外発生時
			model.addAttribute("errorMessage", "DB接続時にエラーが発生しました。時間を空けて再度実行してください。");
			LogUtil.error("E10001");
			return "attendance/management/restore";
		}

		LogUtil.info("[{}]:Restore success(shain_id = {})", this.getClass().getSimpleName(), request.getShainId());
		return "redirect:/attendance/management/worktable";
	}

	/**
	 * 復旧処理を行わずに、現在の勤務表（管理テーブル）画面へ戻る遷移を制御します。
	 *
	 * @return 勤務表画面へのリダイレクト指示
	 */
	@PostMapping(value = "/attendance/management/back_table")
	public String backToTable() {
		return "redirect:/attendance/management/worktable";
	}
}