package com.example.attendance.controller;

import java.util.stream.Collectors;

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

import com.example.attendance.dto.LoginRequest;
import com.example.attendance.entity.ShainData;
import com.example.attendance.service.LoginService;
import com.example.attendance.util.ControllerUtil;
import com.example.attendance.util.LogUtil;
import com.example.attendance.util.MessagesPropertiesUtil;

/**
 * ログイン画面に関する画面表示および認証リクエストを制御するコントローラークラス。
 * <p>
 * ユーザーからのログイン処理（POSTリクエスト）を受け付け、サービス層の認証結果に応じて
 * 適切なエラーコードの判定を行います。特に認証失敗時（case 0）には、入力値の中身を精査し、
 * 「ID未入力」「パスワード未入力」「情報不一致」の各パターンへ緻密にエラー表示を切り分ける責務を持ちます。
 *  @author Soeda
 *  @version 2.0 2026-06-17 kato
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
	 * <b>【認証結果による制御フロー】</b>
	 * <ul>
	 * <li>{@code 1}（バインドエラー）:DBアクセスよりも前に未入力チェックを行います。
	 * <ul>
	 * <li><b>パターンA（ID未入力）:</b> 警告ログ（{@code W10001}）を出力し、ID入力を促します。</li>
	 * <li><b>パターンB（パスワード未入力）:</b> 警告ログ（{@code W10002}）を出力し、パスワード入力を促します。</li>
	 * </ul>
	 * </li>
	 * <li>{@code 2} （ID該当なし）:</b>警告ログ （{@code W10006}）を出力し、入力されたIDが存在しないメッセージを設定。</li>
	 * <li>{@code 3} （停止アカウント）:警告ログ（{@code W10005}）を出力し、停止済みメッセージを設定して画面へ戻します。</li>
	 * <li>{@code 4} （パスワード不一致）:DBのfailuer_countを増加させた後、残り試行可能回数に応じて下記のいずれかに派生します。
	 * <ul>
	 * <li><b>パターンA（試行可能回数1以上）:</b>警告ログ（{@code W10003}）を出力し、不一致メッセージを設定します。</li>
	 * <li><b>パターンB（試行可能回数0）:</b>警告ログ（{@code W10004}）を出力し、3回失敗による停止メッセージを設定して画面へ戻します。</li></ul></li>
	 * <li>{@code 5} （成功）:failuer_countを0に更新し、勤務表画面（{@code /attendance/management/worktable}）へリダイレクトします。</li>
	 * <li>{@code 6} （DB接続失敗）:</b>エラーログ（{@code E10001}）を出力し、何らかのエラーでDBへ接続できなかったメッセージを設定。</li>
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @param loginRequest  ログイン画面のフォームから送信されたIDとパスワードが格納されたDTO
	 * @param bindingResult バリデーション結果を保持するオブジェクト
	 * @param model         画面にエラーメッセージや入力データを保持させるためのModelオブジェクト
	 * @return 認証成功時は勤務表画面へのリダイレクト指示、失敗時はログイン画面のテンプレートパス
	 */
	@PostMapping("/attendance/management/login")
	public String login(@ModelAttribute("loginRequest") @Validated LoginRequest loginRequest,
			BindingResult bindingResult, Model model) {

		//バインディングチェック
		if (bindingResult.hasErrors()) {
			ControllerUtil.warnAllBindErrors(bindingResult,
					LoginRequest.getAnnotationCodeMap());
			model.addAttribute("errorMessage", bindingResult.getFieldErrors().stream()
					//メッセージを取得してListに変換
					.map(x -> x.getDefaultMessage())
					.collect(Collectors.toList()));
			return "attendance/management/login";
		}

		//ログイン処理
		try {
			String errorcode = null;
			ShainData shain = loginService.getShainById(loginRequest.getLoginId());

			//入力されたID・パスワードを検証
			if (shain == null) {
				//shainが獲得できない=社員IDが有効でない場合
				errorcode = "W10006";
			} else if (shain.getStopFlg() != 0) {
				//ストップフラグが0でない=凍結されている場合
				errorcode = "W10005";
			} else if (!shain.getPassword().equals(loginRequest.getPassword())) {
				//社員のパスワードが一致しない＝パスワードを間違えた場合
				//DB更新
				loginService.incrementCount(shain);

				int remainFailure = 3 - (shain.getFailureCount() + 1); //残りの試行可能回数
				if (remainFailure > 0) {
					//試行可能回数がまだ残っている場合
					LogUtil.warn("W10003");
					String message = MessagesPropertiesUtil.getErrorMessage("W10003") + " (残り: " + remainFailure + "回)";
					model.addAttribute("loginRequest", loginRequest);
					model.addAttribute("errorMessage", message);
					return "attendance/management/login";
				}
				//ちょうど試行可能回数が0になった場合
				errorcode = "W10004";
			}

			//いずれかのチェックに引っかかった場合はwarnを出して戻る
			if (errorcode != null) {
				LogUtil.warn(errorcode);
				model.addAttribute("loginRequest", loginRequest);
				model.addAttribute("errorMessage", MessagesPropertiesUtil.getErrorMessage(errorcode));
				return "attendance/management/login";
			}

			//DBおよびインスタンスの更新
			shain = loginService.resetCountBothDbAndShainData(shain);
			LogUtil.info("[正常なログイン]ユーザー:{}(社員ID:{}, ログインID:{})",
					shain.getShainName(),
					shain.getShainId(),
					shain.getLoginId());
			session.setAttribute("loginShain", shain);
			return "redirect:/attendance/management/worktable";

		} catch (DataAccessException e) {
			//データベースアクセスのどこかでエラーが発生した場合
			LogUtil.error("E10001");
			model.addAttribute("errorMessage", MessagesPropertiesUtil.getErrorMessage("E10001"));
			return "attendance/management/login";
		}
	}
}