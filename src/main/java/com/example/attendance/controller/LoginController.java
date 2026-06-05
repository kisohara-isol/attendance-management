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
 * <p>
 * ユーザーからのログイン処理（POSTリクエスト）を受け付け、サービス層の認証結果に応じて
 * 適切なエラーコードの判定を行います。特に認証失敗時（case 0）には、入力値の中身を精査し、
 * 「ID未入力」「パスワード未入力」「情報不一致」の各パターンへ緻密にエラー表示を切り分ける責務を持ちます。
 *  @author Soeda
 * </p>
 */
@Controller
public class LoginController {

	/**
	 * ログイン認証の判定や失敗回数のカウント管理を行うサービス
	 */
	@Autowired
	private LoginService loginService;

	@Autowired
	private HttpSession session;

	/**
	 * ログイン画面の初期表示を行います。
	 * <p>
	 * 画面遷移のログとして、システムエラーコード（{@code E99999}）をログに出力したうえで、
	 * ログイン表示用テンプレートへと案内します。
	 * </p>
	 *
	 * @param model 画面（Thymeleafテンプレート）にデータを渡すためのModelオブジェクト
	 * @return ログイン画面のテンプレートパス ({@code "attendance/management/login"})
	 *
	 */
	@GetMapping(value = "/attendance/management/login")
	public String display(@ModelAttribute("loginRequest") LoginRequest loginRequest, Model model) {
		return "attendance/management/login";
	}

	/**
	 * ログイン画面で「ログイン」ボタンが押された際、認証判定と詳細なエラーハンドリングを行います。
	 * <p>
	 * <b>【認証結果による制御フロー（ステータスコード）】</b>
	 * <ul>
	 * <li>{@code 1} (成功): 勤務表画面（{@code /attendance/management/worktable}）へリダイレクトします。</li>
	 * <li>{@code 2} (既ロック): 警告ログ（{@code W10005}）を出力し、停止済みメッセージを設定して画面へ戻します。</li>
	 * <li>{@code 3} (今ロック): 警告ログ（{@code W10004}）を出力し、3回失敗による新規ロックメッセージを設定して画面へ戻します。</li>
	 * <li>{@code 4} (通常失敗):</b>警告ログ ({@code W10006})を出力し、入力されたIDが存在しないメッセージを設定。</li>
	 * <li>{@code 5} (DB接続失敗):</b>エラーログ({@code E10001})を出力し、何らかのエラーでDBへ接続できなかったメッセージを設定。</li>
	 * <li>{@code 0} (通常失敗 / 初期値): ログイン失敗の中身をさらに以下の3パターンに分岐して処理します。
	 * <ul>
	 * <li><b>パターンA（ID未入力）:</b> 警告ログ（{@code W10001}）を出力し、ID入力を促します。</li>
	 * <li><b>パターンB（パスワード未入力）:</b> 警告ログ（{@code W10002}）を出力し、パスワード入力を促します。</li>
	 * <li><b>パターンC（情報不一致）:</b> 警告ログ（{@code W10003}）を出力し、不一致メッセージを設定します。</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @param loginRequest  ログイン画面のフォームから送信されたIDとパスワードが格納されたDTO
	 * @param model         画面にエラーメッセージや入力データを保持させるためのModelオブジェクト
	 * @param locale        ユーザーのブラウザ環境からSpringが自動判定したロケール（言語・地域情報）
	 * @param bindingResult バリデーション結果を保持するオブジェクト
	 * @return 認証成功時は勤務表画面へのリダイレクト指示、失敗時はログイン画面のテンプレートパス
	 */
	@PostMapping("/attendance/management/login")
	public String login(@ModelAttribute("loginRequest") LoginRequest loginRequest, Model model, Locale locale,
			BindingResult bindingResult) {

		if (loginRequest.getLoginId() == null) {

			LogUtil.warn("W10001");
			model.addAttribute("errorMessage", "IDを入力してください");
			return "/attendance/management/login";
		}

		// ServiceにIDとパスワードを渡し、結果のコードを受け取る
		int statusCode = loginService.loginJudge(loginRequest.getLoginId(), loginRequest.getPassword());

		// 失敗した時に備えて、入力データをModelに保持しておく
		model.addAttribute("loginRequest", loginRequest);

		switch (statusCode) {
		case 1:
			ShainData loginShain = loginService.getShainById(loginRequest.getLoginId());
			session.setAttribute("loginShain", loginShain);
			// ログイン成功
			return "redirect:/attendance/management/worktable";

		case 2:
			// すでにアカウントが停止されている場合
			LogUtil.warn("W10005");
			model.addAttribute("errorMessage", "このアカウントは停止されています。管理者に問い合わせてください。");
			return "/attendance/management/login";

		case 3:
			// 今回の失敗で新しくアカウントが停止された場合
			LogUtil.warn("W10004");
			model.addAttribute("errorMessage", "ログイン失敗が3回に達したため、アカウントを停止しました。");
			return "/attendance/management/login";

		case 4:
			//ログインした際社員IDが存在しない場合
			LogUtil.warn("W10006");
			model.addAttribute("errorMessage", "この社員IDは存在しません");
			return "/attendance/management/login";

		case 5:
			//何かしたのエラーでDBに接続できなかった場合
			LogUtil.error("E10001");
			model.addAttribute("errorMessage", "DB接続時にエラーが発生しました。時間を空けて再度実行してください。");
			return "/attendance/management/login";

		case 0:

		default: // ⭕ ログイン失敗（case 0）の中で、未入力と間違いパターンを切り分ける

			int remaining = loginService.getRemainingAttempts(loginRequest.getLoginId());

			if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
				// パターンA：passwordが何も書かれていない状態のエラー
				LogUtil.warn("W10002");
				model.addAttribute("errorMessage", "パスワードを入力してください。（残り: " + remaining + "回）");

			} else {
				// パターンB：入力して間違えているパターン
				LogUtil.warn("W10003");
				model.addAttribute("errorMessage", "ログインIDまたはパスワードが間違っています。（残り: " + remaining + "回）");
			}

			return "/attendance/management/login";
		}
	}
}