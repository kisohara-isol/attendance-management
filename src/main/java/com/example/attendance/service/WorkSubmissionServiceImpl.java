package com.example.attendance.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.attendance.entity.AttendanceData;
import com.example.attendance.entity.SalaryData;
import com.example.attendance.repository.WorkSubmissionMapper;
import com.example.attendance.util.LogUtil;
/**
 * 勤怠データの集計、および給与計算処理を行うビジネスロジックの実装クラスです。
 * <p>
 * 月間の所定労働日数・実労働日数の集計、有給休暇のカウントをはじめ、
 * 基本給や各種割増手当（残業・休日・深夜）、欠勤控除を考慮した厳密な給与計算を行います。
 * また、計算結果を特定の固定長テキスト形式のCSVファイルとして出力する機能を備えています。
 * </p>
 */
@Service
public class WorkSubmissionServiceImpl implements WorkSubmissionService {

	@Autowired
	WorkSubmissionMapper mapper;

	// String型の出勤日をLocalDat型に直すためのフォーマット
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
	
	/**
	 * 指定された勤怠データリストから、所定労働日数（平日のうち祝日を除いた日数）を取得します。
	 *
	 * @param workList 勤怠データのリスト
	 * @return 所定労働日数（日）
	 */
	@Override
	public int getAllWorkDay(List<AttendanceData> workList) {		
		// リストから平日かつ祝日でもない日を取得
		long count = workList.stream().filter(a -> !a.getDayOfWeek().equals("土") && !a.getDayOfWeek().equals("日")
				&& Holiday.judgeHoliday(LocalDate.parse(a.getWorkDay(), dtf)) == null).count();

		return (int) count;
	}

	/**
	 * 指定された勤怠データリストから、実際の出勤日数を取得します。
	 * <p>
	 * 出勤時間が1分でも存在する、実際に稼働した日数をカウントします。
	 * </p>
	 *
	 * @param workList 勤怠データのリスト
	 * @return 実労働日数（日）
	 */
	@Override
	public int getActualWorkDay(List<AttendanceData> workList) {
		// 休み(勤怠で登録したものと、土日祝日を含む)を除いた日をリスト化
		long count = workList.stream().filter(a -> a.getMinutes() > 0).count();
		return (int) count;
	}

	/**
	 * 指定された勤怠データリストから、有給休暇の取得日数を取得します。
	 * <p>
	 * 出勤時間が0分かつ、土日祝以外の日に備考欄に「有給」と記載されている日を対象とします。
	 * </p>
	 *
	 * @param workList 勤怠データのリスト
	 * @return 有給休暇の取得日数（日）
	 */
	@Override
	public int getPaidHoliday(List<AttendanceData> workList) {
		// 出勤時間が0分かつ土日祝でなく、備考欄に「有給」と書いてある日を合算
		long count = workList.stream().filter(a -> "有給".equals(a.getNote()) && a.getMinutes() == 0 && Holiday.judgeHoliday(LocalDate.parse(a.getWorkDay(), dtf)) != null && !a.getDayOfWeek().equals("土") && !a.getDayOfWeek().equals("日")).count();
		return (int) count;
	}

	/**
	 * 指定された社員の月間総支給額（給与）を計算します。
	 * <p>
	 * データベースから取得した各種給与単価を基に、以下の計算式に従って支給額を算出します：<br>
	 * <code>総支給額 = 基本給 + 深夜手当の合計 + 残業・休日手当の合計 - 欠勤控除額</code>
	 * </p>
	 * * <b>【手当・控除の計算仕様】</b>
	 * <ul>
	 * <li><b>残業手当：</b> 残業時間（分）を時間換算し、<code>時間単価 × 残業単価倍率</code> を乗算します。</li>
	 * <li><b>休日手当：</b> 法定休日等の出勤時間（分）を時間換算し、<code>時間単価 × (休日手当倍率 - 1)</code>
	 * 分を割増分として加算します。</li>
	 * <li><b>深夜手当：</b>
	 * 22:00から翌5:00までの勤務時間を1分単位で厳密にカウントし、<code>時間単価 × (深夜手当倍率 - 1)</code>
	 * 分の割増手当を加算します。退勤時間が24時を超える跨ぎ勤務にも対応しています。</li>
	 * <li><b>欠勤控除：</b>
	 * 「出勤日の1日8時間（480分）に満たない不足時間」および「平日の丸一日欠勤（有給を除く）」の合計欠勤時間を算出し、<code>時間単価</code>
	 * を乗じて控除します。</li>
	 * </ul>
	 *
	 * @param shainId  計算対象の社員ID
	 * @param workList 対象月の勤怠データリスト ({@link AttendanceData} のリスト)
	 * @return 計算された総支給額（円単位、小数点以下切り捨て）
	 */
	@Override
	public int getAllSalary(int shainId, List<AttendanceData> workList) {
		SalaryData sd = mapper.selectSalaryData(shainId);
		LogUtil.info("データベース[salary_components]にアクセスしました。");
		// 基本給＋残業時間＊残業単価ー欠勤時間＊時間単価＋深夜勤務時間＊深夜手当＋休日勤務時間＊休日手当

		// 基本給
		int baseSalary = sd.getBaseSalary();

		// 残業単価
		double overTimeBonus = sd.getOverTimeBonus();

		// 時間単価
		int timeCost = sd.getTimeCost();

		// 深夜手当(22～5時)
		double lateNightBonus = sd.getLateNightBonus();

		// 休日手当
		double holidayBonus = sd.getHolidayBonus();
		// 残業・休日手当
		double totalOverBonus = 0;
		// 深夜手当の合計額
		double totalLateNightBonus = 0;

		// 手当の代を計算する
		for (AttendanceData ad : workList) {
			// 休日出勤
			boolean isBreak = ad.isBreakDay();
			if (isBreak) {
				totalOverBonus += ((double) ad.getMinutes() / 60.0) * timeCost * (holidayBonus - 1);
			}

			// 残業手当
			long overMinutes = ad.getOverMinutes();
			if (overMinutes > 0) {
				totalOverBonus += ((double) ad.getOverMinutes() / 60.0) * timeCost * overTimeBonus;
			}

			if (ad.getStartTime() == null || ad.getEndTime() == null) {
				// 出勤時間または退勤時間が空（休みの日など）の場合は、深夜手当の計算をスキップして次の日のデータへ
				continue;
			}

			// 出勤時間・退勤時間
			LocalDate workDay = LocalDate.parse(ad.getWorkDay(), dtf);
			LocalDateTime startTime = LocalDateTime.of(workDay, ad.getStartTime());
			LocalDateTime endTime = null;
			int endHour = Integer.parseInt(ad.getEndTime().split(":")[0]);
			int endMinutes = Integer.parseInt(ad.getEndTime().split(":")[1]);

			// 退勤時間が24時を過ぎた場合、次の日に繰り越す
			if (endHour > 24) {
				endTime = LocalDateTime.of(workDay.plusDays(1), LocalTime.of(endHour - 24, endMinutes));
			} else {
				endTime = LocalDateTime.of(workDay, LocalTime.of(endHour, endMinutes));
			}

			// 深夜勤務の時間
			long allLateNightMinutes = 0; // 全ての深夜勤務
			LocalDateTime checkTime = startTime;

			// 1分ずつ進めながら、勤務終了までループ
			while (checkTime.isBefore(endTime)) {
				int hour = checkTime.getHour();

				// 22時以降、または朝5時より前か判定
				if (hour >= 22 || hour < 5) {
					allLateNightMinutes++; // 深夜勤務時間を1分測る
				}
				// 1分進める
				checkTime = checkTime.plusMinutes(1);
			}

			// 深夜帯が含まれていた時加算
			totalLateNightBonus += ((double) allLateNightMinutes / 60.0) * timeCost * (lateNightBonus - 1);
		}
		// 欠勤時間
		long shortMinutes = 0;
		for (AttendanceData ad : workList) {
			long minutes = ad.getMinutes();
			// 出勤したのに8時間（480分）未満だった場合
			if (minutes > 0 && minutes < 480) {
				shortMinutes += 480 - minutes;
			}
		}
		// 欠勤控除
		double shortMoney = 0;
		double moreMoney = 0; // 超過勤務

		int allWorkDay = getAllWorkDay(workList); // 出勤対象日数
		int actualWorkDay = getActualWorkDay(workList); // 出勤日数
		int paidHoliday = getPaidHoliday(workList); // 有給
		if (allWorkDay > actualWorkDay) {
			// 欠勤日数（平日に丸一日休んだ日数）の計算
			// 有給は出勤扱いにする
			int absentDays = allWorkDay - actualWorkDay - paidHoliday;
			if (absentDays > 0) {
				shortMinutes += absentDays * 480;
			}
		} else if(allWorkDay <= actualWorkDay) {
			// 出勤日数が出勤対象日数を上回った場合、上回った分だけ加算する
			int moreDays = actualWorkDay - allWorkDay + paidHoliday;
			moreMoney += moreDays * 8 * timeCost;
		}
		shortMoney = ((double) shortMinutes / 60.0) * timeCost; // 欠勤控除の計算
		// 基本給 ＋ 残業・休日手当 ＋ 深夜手当 ー 欠勤控除 ＋ 超過勤務
		double finalSalaryAmount = baseSalary + totalLateNightBonus + totalOverBonus - shortMoney + moreMoney;

		return (int) finalSalaryAmount;
	}

	/**
	 * 指定された社員の給与情報を固定長テキスト形式でCSVファイルとして生成します。
	 * <p>
	 * 出力されるファイルは「C:\workComplete\」ディレクトリ配下に、 「ログインID_年月_salary.csv」の形式で保存されます。
	 * 書き込まれるデータ（社員名、実労働日数、総支給額）は、指定されたバイト長に達するまで 半角スペースが末尾に補填されます。
	 * </p>
	 *
	 * @param yearMonth     対象年月 (例: "202606")
	 * @param actualWorkDay 実労働日数
	 * @param allSalary     総支給額
	 * @param shainName     社員名
	 * @param loginId       ログインID
	 * @return 生成された CSV ファイルの名前
	 * @throws IllegalArgumentException ファイルの新規作成、または書き込み時に {@link IOException}
	 *                                  が発生した場合
	 */
	@Override
	public String createFile(String yearMonth, int actualWorkDay, int allSalary, String shainName, String loginId) {
		String fileName = loginId + "_" + yearMonth + "_" + "salary.csv"; // ファイル名
		// ファイル作成
		File file = new File("C:\\workComplete\\" + fileName);

		// ファイルの中身作成
		String fileWrite = addSpace(shainName, 20) + addSpace(String.valueOf(actualWorkDay), 2)
				+ addSpace(String.valueOf(allSalary), 10);

		// ファイルの中身に書き込み
		BufferedWriter bw = null;
		try {
			file.createNewFile();
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			bw.write(fileWrite);
			bw.flush();
		} catch (IOException e) {
			throw new IllegalArgumentException("ファイル読み込みエラーです。終了します", e);
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return fileName;
	}

	/**
	 * 指定された文字列の末尾に半角スペースを補填し、指定されたバイト長に調整した文字列を返します。
	 * <p>
	 * 文字列の現在のバイト長を基準に計算するため、全角文字（日本語など）が含まれる場合は 環境のデフォルトエンコーディングにおけるバイト数で計算されます。
	 * </p>
	 *
	 * @param str        スペースを補填する対象の文字列
	 * @param byteLength 調整した後の目標バイト長
	 * @return 末尾に半角スペースが補填された文字列
	 */
	public static String addSpace(String str, int byteLength) {
		// 現在のバイト数
		int strLength = str.getBytes().length;
		StringBuilder sb = new StringBuilder();

		// 目標のバイト数になるよう半角スペースで調整する
		sb.append(str);
		for (int i = 0; i < byteLength - strLength; i++) {
			sb.append(" ");
		}

		return sb.toString();
	}

}
