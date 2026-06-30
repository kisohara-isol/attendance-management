package com.example.attendance.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.attendance.entity.ShainData;
import com.example.attendance.service.WorkComleteServiceImpl;
import com.example.attendance.util.LogUtil;

/**
 * 勤務確定（完了）画面の表示、および給与明細ファイルのダウンロードを制御するコントローラークラス。
 * <p>
 * 勤務表の確定処理後に遷移し、その月の総実績や概算給与の確認、
 * および外部システム連携用の固定長CSVデータのダウンロード機能を提供します。
 * </p>
 *
 * @author kato (or Soeda)
 */
@Controller
public class WorkComleteController {

	/** 勤務確定およびファイル生成用のビジネスロジックを提供するサービス実装クラス */
	@Autowired
	private WorkComleteServiceImpl comlete;

	/**
	 * 勤務確定の完了画面（HTML）を表示します。
	 * <p>
	 * セッションからログイン中の社員情報を取得し、確定された年月の実績データをModel経由で画面に渡します。
	 * セッションが失われている場合はログイン画面へ強制リダイレクトします。
	 * </p>
	 *
	 * @param model          画面へパラメータを渡すためのModelオブジェクト
	 * @param session        ログインユーザー情報を検証するためのHTTPセッション
	 * @param workYear       確定対象の年
	 * @param workMonth      確定対象の月
	 * @param attendanceDate その月の実際の出勤日数
	 * @param salary         計算された支給総額（給与）
	 * @return 完了画面のHTMLテンプレートパス、またはログイン画面へのリダイレクト
	 */
	@GetMapping("/attendance/management/workcomlete")
	public String display(Model model, HttpSession session,
			@RequestParam("Year") int workYear,
			@RequestParam("Month") int workMonth,
			@RequestParam("attendanceDate") int attendanceDate,
			@RequestParam("salary") int salary) {

		// 💡【安全化】セッション切れチェックを徹底（ぬるぽクラッシュ防御）
		if (session == null || session.getAttribute("loginShain") == null) {
			LogUtil.warn("W99999");
			return "redirect:/attendance/management/login";
		}
		ShainData shain = (ShainData) session.getAttribute("loginShain");

		// 画面（HTML）側で使えるようにModelへ格納する
		model.addAttribute("login_id", shain.getLoginId());
		model.addAttribute("yyyy", workYear);
		model.addAttribute("MM", workMonth);
		model.addAttribute("attendanceDate", attendanceDate);
		model.addAttribute("salary", salary);

		return "attendance/management/workcomlete";
	}

	/**
	 * 確定された給与明細データをCSV（固定長テキスト形式のバイト配列）としてブラウザにダウンロードさせます。
	 * <p>
	 * このメソッドは画面遷移を行わず、レスポンスのBodyに直接ファイルのバイナリを乗せて応答します。
	 * ファイル名は「ログインID_年月_salary.csv」として出力されます。
	 * </p>
	 *
	 * @param session        ログインユーザーのIDを取得するためのHTTPセッション
	 * @param workYear       出力対象の年
	 * @param workMonth      出力対象の月
	 * @param attendanceDate 出勤日数
	 * @param salary         支給総額
	 * @return ファイルのバイナリデータを含むレスポンスエンティティ
	 */
	@GetMapping("/attendance/management/workcomlete/download")
	public ResponseEntity<byte[]> download(HttpSession session,
			@RequestParam("Year") int workYear,
			@RequestParam("Month") int workMonth,
			@RequestParam("attendanceDate") int attendanceDate,
			@RequestParam("salary") int salary) {

		// 💡【安全化】ダウンロードボタン押下時のセッション切れも完全に防御
		if (session == null || session.getAttribute("loginShain") == null) {
			LogUtil.warn("W99999");
			// ファイルダウンロード用URLのため、HTTPステータス（UNAUTHORIZED）で返却するか、安全な空データを返します
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		ShainData shain = (ShainData) session.getAttribute("loginShain");

		// 1. Serviceを呼び出して、固定長データのバイト配列をもらう
		byte[] fileBytes = comlete.createFile(shain, workYear, workMonth, attendanceDate, salary);

		// 2. ブラウザへのダウンロードへの出力設定
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		// ファイル名を設定（月を2桁固定 "01", "12" に整形してバグ防止）
		String fileName = shain.getLoginId() + "_" + workYear + String.format("%02d", workMonth) + "_salary.csv";
		headers.setContentDispositionFormData("attachment", fileName);

		// 3. 画面遷移はせず、ファイルデータそのものを応答（Response）として返却する
		return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
	}
}