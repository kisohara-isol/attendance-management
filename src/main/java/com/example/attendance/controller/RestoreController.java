package com.example.attendance.controller;

import java.util.Collections;

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
 * /attendance/management/restoreのコントローラ
 * @author kato
 */
@Controller
public class RestoreController {

	/**サービスクラス*/
	@Autowired
	private RestoreService restoreService;

	/**
	 * /attendance/management/restoreの描写
	 * @param restoreRequest 画面の入力
	 * @param session "loginShain"に値が存在する(管理者のShainDataであることが期待される)
	 * @param redirect
	 * @return /attendance/management/restore
	 */
	@GetMapping("/attendance/management/restore")
	public String display(@ModelAttribute RestoreRequest restoreRequest, HttpSession session,
			RedirectAttributes redirect) {
		//sessionの確認
		boolean isKeepingSession = Collections.list(session.getAttributeNames()) //sessionに保存されている中身の名前をlistに
				.stream().anyMatch(x -> "loginShain".equals(x)); //streamで"loginShain"の存在を確認
		if (!isKeepingSession) {
			LogUtil.warn("W99999");
			redirect.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");
			return "redirect:/attendance/management/login"; // 最初のページへ
		}
		return "/attendance/management/restore";
	}

	/**
	 * HTML側で入力された社員IDに該当するアカウントのロックを解除して、勤務表画面へ移行する。<br>
	 * 入力値が不正だった場合やDB更新が正常になされなかった場合は、ページ遷移せずエラー文を表示する。
	 * @param request 社員IDの入ったdto
	 * @param result バリデーションの結果
	 * @param session "loginShain"に値が存在する(管理者のShainDataであることが期待される)
	 * @param model DB更新が正常に終わらなかった場合、"errorMessage"にメッセージが入る
	 * @param redirect 
	 * @return 成功した場合/attendance/management/worktableへのリダイレクト
	 */
	@PostMapping("/attendance/management/restore")
	public String restoreAndRedirect(@ModelAttribute @Validated RestoreRequest request, BindingResult result,
			HttpSession session, Model model, RedirectAttributes redirect) {
		//sessionの確認(再度)
		boolean isKeepingSession = Collections.list(session.getAttributeNames()) //sessionに保存されている中身の名前をlistに
				.stream().anyMatch(x -> "loginShain".equals(x)); //streamで"loginShain"の存在を確認
		if (!isKeepingSession) {
			LogUtil.warn("W99999");

			redirect.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");

			return "redirect:/attendance/management/login"; // 最初のページへ
		}

		//バリデーションチェック
		if (result.hasErrors()) {
			//バリデーションに引っかかった場合
			result.getFieldErrors().forEach(x -> LogUtil.warn(RestoreRequest.getErrorCode(x.getCode())));
			return "attendance/management/restore";
		}

		//DBの更新
		try {
			if (!restoreService.executeRestoreShain(request.getShainId())) {
				//データ更新が正常になされなかった場合はエラーを表示
				model.addAttribute("errorMessage", "入力した社員IDに該当するロックされたアカウントは存在しません。"); //W40003のメッセージ本文
				LogUtil.warn("W40003");
				return "attendance/management/restore";
			}
		} catch (DataAccessException e) {
			//DBアクセスに失敗した場合もエラーを表示
			model.addAttribute("errorMessage", "DB接続時にエラーが発生しました。時間を空けて再度実行してください。");
			LogUtil.error("E10001");
			return "attendance/management/restore";
		}

		LogUtil.info("[{}]:Restore success(shain_id = {})", this.getClass().getSimpleName(), request.getShainId());
		//勤務表画面へリダイレクト
		return "redirect:/attendance/management/worktable";
	}
}
