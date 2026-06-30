package com.example.attendance.controller;

import java.util.Locale;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.attendance.dto.LoginRequest;
import com.example.attendance.entity.ShainData;
import com.example.attendance.service.LoginService;
import com.example.attendance.util.LogUtil;

/**
 * ログイン画面に関する画面表示および認証リクエストを制御するコントローラークラス。
 * * @author kato
 */
@Controller
public class LoginController {

	/** ログイン認証のビジネスロジックを提供するサービスクラス */
	@Autowired
	private LoginService loginService;

	/** ログイン成功時のユーザー情報保持などに利用するHTTPセッション */
	@Autowired
	private HttpSession session;

	/**
	 * ログイン画面の初期表示を行います。
	 * <p>
	 * 画面の入力値をバインドするための空の {@link LoginRequest} をModelに設定し、
	 * ログインページのHTMLを返します。
	 * </p>
	 *
	 * @param loginRequest 画面のフォームデータを格納するDTOオブジェクト
	 * @param model        画面へデータを渡すためのModelオブジェクト
	 * @return ログイン画面のHTMLパス ("attendance/management/login")
	 */
	@GetMapping(value = "/attendance/management/login")
	public String display(@ModelAttribute("loginRequest") LoginRequest loginRequest, Model model) {
		return "attendance/management/login";
	}

	/**
	 * 画面から送信されたログイン情報を検証し、認証処理を行います。
	 * <p>
	 * 入力されたIDおよびパスワードを基にステータスコードを判定し、以下の遷移を行います。
	 * <ul>
	 * <li>認証成功 (code: 1): セッションにユーザー情報を保持し、勤務表照会画面へリダイレクトします。</li>
	 * <li>アカウントロック中 (code: 2, 3): ロック通知メッセージを設定し、ログイン画面へ戻ります。</li>
	 * <li>入力不備・認証失敗 (code: 4, 5, 0): 各種エラーメッセージを設定し、ログイン画面へ戻ります。</li>
	 * </ul>
	 * </p>
	 *
	 * @param loginRequest  画面から送信されたログイン情報（ID、パスワード）が格納されたDTO
	 * @param model         画面へエラーメッセージやリクエストデータを渡すためのModelオブジェクト
	 * @param locale        ロケール情報（多言語化対応用、現状は不使用）
	 * @param bindingResult バリデーションエラーの結果を保持するオブジェクト
	 * @return 認証成功時は勤務表照会へのリダイレクト、失敗時はログイン画面のHTMLパス
	 */
	@PostMapping("/attendance/management/login")
	public String login(@ModelAttribute("loginRequest") LoginRequest loginRequest, Model model, Locale locale,
			BindingResult bindingResult) {

		// ログインIDの未入力チェック
		if (loginRequest.getLoginId() == null || loginRequest.getLoginId().trim().isEmpty()) {
			LogUtil.warn("W10001");
			model.addAttribute("errorMessage", "IDを入力してください");
			// 💡【修正】先頭の「/」を削除してThymeleafの解析エラー（画面真っ白）を防止
			return "attendance/management/login";
		}

		// サービス層でログイン可否の判定を行い、結果に応じたステータスコードを取得
		int statusCode = loginService.loginJudge(loginRequest.getLoginId(), loginRequest.getPassword());

		// 入力された情報を画面に引き継ぐ
		model.addAttribute("loginRequest", loginRequest);

		switch (statusCode) {
		case 1:
			// 認証成功：ユーザー情報を取得してセッションに格納し、勤務表へ遷移
			ShainData loginShain = loginService.getShainById(loginRequest.getLoginId());
			session.setAttribute("loginShain", loginShain);
			return "redirect:/attendance/management/worktable";

		case 2:
			// 停止済みアカウントによるアクセス
			LogUtil.warn("W10005");
			model.addAttribute("errorMessage", "このアカウントは停止されています。管理者に問い合わせてください。");
			return "attendance/management/login";

		case 3:
			// 今回の失敗でちょうど3回に達し、アカウントが自動ロックされた場合
			LogUtil.warn("W10004");
			model.addAttribute("errorMessage", "ログイン失敗が3回に達したため、アカウントを停止しました。");
			return "attendance/management/login";

		case 4:
			// 存在しない社員ID
			LogUtil.warn("W10006");
			model.addAttribute("errorMessage", "この社員IDは存在しません");
			return "attendance/management/login";

		case 5:
			// データベース接続エラーなどのシステム例外
			LogUtil.error("E10001");
			model.addAttribute("errorMessage", "DB接続時にエラーが発生しました。時間を空けて再度実行してください。");
			return "attendance/management/login";

		case 0:
		default:
			// 通常の認証失敗（パスワード間違いなど）：残り試行回数を算出してメッセージを表示
			int remaining = loginService.getRemainingAttempts(loginRequest.getLoginId());

			// 特定アカウント（kisohara）用の特殊メッセージ制御
			if (loginRequest.getLoginId().equals("kisohara")) {
				LogUtil.warn("W10003");
				model.addAttribute("errorMessage", "ログインIDまたはパスワードが間違っています。");
				return "attendance/management/login";
			}

			if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
				// パスワード未入力
				LogUtil.warn("W10002");
				model.addAttribute("errorMessage", "パスワードを入力してください。（残り: " + remaining + "回）");
			} else {
				// パスワード誤り
				LogUtil.warn("W10003");
				model.addAttribute("errorMessage", "ログインIDまたはパスワードが間違っています。（残り: " + remaining + "回）");
			}

			return "attendance/management/login";
		}
	}
}