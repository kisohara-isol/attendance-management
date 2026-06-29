package com.example.attendance.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.attendance.util.ControllerUtil;
import com.example.attendance.util.LogUtil;
import com.example.attendance.util.MessagesPropertiesUtil;

/**
 * workcompleteのコントローラ
 */
@Controller
public class WorkCompleteController {
	/**
	 * リダイレクト後、ファイル名(fileName)とファイルの内容(contents)を受け取り、<br>
	 * 両方をモデルとセッションに詰めた後描写する。
	 * @param fileName ファイル名
	 * @param contents ファイルの内容
	 * @param session 社員名に加えて、ファイル名とファイルの内容をあたえる
	 * @param model ファイル名をhtml側でも描写する
	 * @param redirect
	 * @return /attendance/management/workcompleteの描写
	 */
	@GetMapping("/attendance/management/workcomplete")
	public String display(@ModelAttribute("fileName") String fileName,
			@ModelAttribute("contents") String contents,
			HttpSession session,
			Model model,
			RedirectAttributes redirect) {
		System.out.println(model.asMap());

		if (!ControllerUtil.isKeepingSession(session, "loginShain")) {
			LogUtil.warn("W99999");
			redirect.addFlashAttribute("errorMessage", "セッションの有効期限が切れました。再度ログインしてください。");
			return "redirect:/attendance/management/login"; // 最初のページへ
		}

		session.setAttribute("fileName", fileName);
		session.setAttribute("contents", contents);
		return "/attendance/management/workcomplete";
	}

	/**
	 * 呼び出されると、セッションに保存されていたファイル名(fileName)とファイルの内容(contents)を使って<br>
	 * ファイルを作成、ダウンロードする。
	 * @param session fileNameとcontentsが入ったセッション。実行後この二つは取り除かれる。
	 * @param response このメソッドが発行するレスポンス。ボディにファイルが含まれる
	 * @param redirect
	 * @return ファイル作成に失敗した場合だけworktableに移動する
	 */
	@GetMapping("/attendance/management/workcomplete/download")
	public String downloadCSV(HttpSession session, HttpServletResponse response, RedirectAttributes redirect) {
		//セッションから取り出す
		String fileName = (String) session.getAttribute("fileName");
		String contents = (String) session.getAttribute("contents");

		if (fileName == null || contents == null) {
			LogUtil.warn("W70001");
			redirect.addFlashAttribute("abnormalredirect", MessagesPropertiesUtil.getErrorMessage("W70001"));
			return "redilect:/attendance/management/worktable";
		}
		//取り出した後はセッションを削除
		session.removeAttribute("fileName");
		session.removeAttribute("contents");

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
			LogUtil.warn("W70001");
			redirect.addFlashAttribute("abnormalredirect", MessagesPropertiesUtil.getErrorMessage("W70001"));
			return "redilect:/attendance/management/worktable";
		}
		return null;
	}
}
