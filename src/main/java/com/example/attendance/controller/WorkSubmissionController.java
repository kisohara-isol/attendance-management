package com.example.attendance.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.attendance.dto.WorkResultInformation;
import com.example.attendance.entity.AttendanceData;
import com.example.attendance.entity.ShainData;
import com.example.attendance.service.WorkSubmissionService;
import com.example.attendance.util.ControllerUtil;
import com.example.attendance.util.LogUtil;

/** /attendance/management/worksubmissionのコントローラ*/
@Controller
public class WorkSubmissionController {
	/**DB接続やビジネスロジックを担うサービスクラス*/
	@Autowired
	WorkSubmissionService service;

	/**
	 * /attendance/management/worksubmissionの描写メソッド
	 * @param attendanceList 前ページから持ってきた、ある一か月の完全な勤務情報リスト
	 * @param model
	 * @param session
	 * @param redirect
	 * @return 情報の取得に成功すれば、/attendance/management/worksubmissionの表示
	 */
	@GetMapping(value = "/attendance/management/worksubmission")
	public String display(@ModelAttribute("attendanceList") ArrayList<AttendanceData> attendanceList,
			Model model,
			HttpSession session,
			RedirectAttributes redirect) {

		if (!ControllerUtil.isKeepingSession(session, "loginShain")) {
			LogUtil.warn("W99999");
			redirect.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");
			return "redirect:/attendance/management/login"; // 最初のページへ
		}

		try {
			var workResult = service.aggregateAllAttendances(attendanceList);
			model.addAttribute("workResult", workResult);
		} catch (DataAccessException e) {
			//データベースアクセスのどこかでエラーが発生した場合
			LogUtil.error("E10001");
			//勤務表一覧画面へ戻る
			return "redirect:attendance/management/worktable";
		}
		return "/attendance/management/worksubmission";

	}

	/**
	 * 勤務結果のCSVファイルを作成、出力し、
	 * @param result
	 * @param session
	 * @param model
	 * @param redirect
	 * @param response
	 * @return
	 */
	@PostMapping("/attendance/management/workcomplete")
	public String createCSVAndRedirect(@ModelAttribute("workResult") WorkResultInformation result, HttpSession session,
			Model model, RedirectAttributes redirect,
			HttpServletResponse response) {

		if (!ControllerUtil.isKeepingSession(session, "loginShain")) {
			LogUtil.warn("W99999");
			redirect.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");
			return "redirect:/attendance/management/login"; // 最初のページへ
		}

		ShainData shain = (ShainData) session.getAttribute("loginShain");

		//ファイル名の作成 フォーマットは「${login_id}_yyyymm_salary.csv」
		final String FILE_NAME_FORMAT = "%s_%04d%02d_salary.csv";
		String fileName = FILE_NAME_FORMAT.formatted(shain.getLoginId(), result.getYear(), result.getMonth());

		//ファイルの中身を作成
		//形式は社員名(20B・全角文字は2B換算)勤務日数(2B)給与(10B)の固定長、桁数不測の場合は左寄せして空白埋め 
		final String FILE_CONTENTS_FORMAT = "%s%-2d%-10s";
		String leftAlignedName = shain.getShainName() + " ".repeat(20 - lnegthInByte(shain.getShainName()));
		String contents = FILE_CONTENTS_FORMAT.formatted(
				leftAlignedName,
				result.getActualWorkingDate(),
				result.getGrossSalary());
		//バイト文字列に変換
		byte[] contentsByte = contents.getBytes(Charset.defaultCharset());

		//ファイル出力
		try (OutputStream os = response.getOutputStream()) {
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
			response.setContentLength(contentsByte.length);
			os.write(contentsByte);
			os.flush();
			LogUtil.info("勤務表出力完了[ファイル名:{}]", fileName);
		} catch (IOException e) {
			LogUtil.warn(e.getStackTrace().toString());
		}

		model.addAttribute("fileName", fileName);
		return "redirect:/attendance/management/workcomplete";
	}

	/**
	 * 与えられた文字の文字数を、半角文字は1、全角文字は2として数える
	 * @param str 文字列
	 * @return 全角文字を2文字とした文字数合計
	 */
	private int lnegthInByte(String str) {
		int length = str.codePointCount(0, str.length());
		Matcher mat = Pattern.compile("[^ -~｡-ﾟ]").matcher(str);
		while (mat.find()) {
			//全角文字を見つけるたびにプラス1する
			length++;
		}
		return length;
	}
}
