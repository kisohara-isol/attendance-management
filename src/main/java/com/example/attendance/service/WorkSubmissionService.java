package com.example.attendance.service;

import java.io.IOException;
import java.util.List;

import com.example.attendance.entity.AttendanceData;

/**
 * 社員の勤務表提出、および給与計算に関するビジネスロジックを定義するサービスインターフェースです。
 * <p>
 * 月間の勤怠データ（{@link AttendanceData}）を基に、所定労働日数や実労働日数の算出、
 * 各種手当（残業・休日・深夜）や欠勤控除を加味した月給の計算、
 * および他システム連携用の固定長CSVファイルの作成機能を提供します。
 * </p>
 * * @author Hagiwara
 */
public interface WorkSubmissionService {
	
	/**
	 * 指定された勤怠データリストから、所定労働日数（平日のうち祝日を除いた日数）を取得します。
	 *
	 * @param workList 勤怠データのリスト
	 * @return 所定労働日数（日）
	 */
	int getAllWorkDay(List<AttendanceData> workList);
	
	/**
	 * 指定された勤怠データリストから、実際の出勤日数を取得します。
	 * <p>
	 * 出勤時間が1分でも存在する、実際に稼働した日数をカウントします。
	 * </p>
	 *
	 * @param workList 勤怠データのリスト
	 * @return 実労働日数（日）
	 */
	int getActualWorkDay(List<AttendanceData> workList);
	
	/**
	 * 指定された勤怠データリストから、有給休暇の取得日数を取得します。
	 * <p>
	 * 出勤時間が0分かつ、備考欄に「有給」と記載されている日を対象とします。
	 * </p>
	 *
	 * @param workList 勤怠データのリスト
	 * @return 有給休暇の取得日数（日）
	 */
	int getPaidHoliday(List<AttendanceData> workList);
	
	/**
	 * 指定された社員の月間総支給額（給与）を計算します。
	 * <p>
	 * データベースから取得した各種給与単価を基に、以下の計算式に従って支給額を算出します：<br>
	 * <code>総支給額 = 基本給 + 深夜手当の合計 + 残業・休日手当の合計 - 欠勤控除額</code>
	 * </p>
	 * * <b>【手当・控除の計算仕様】</b>
	 * <ul>
	 * <li><b>残業手当：</b> 残業時間（分）を時間換算し、<code>時間単価 × 残業単価倍率</code> を乗算します。</li>
	 * <li><b>休日手当：</b> 法定休日等の出勤時間（分）を時間換算し、<code>時間単価 × (休日手当倍率 - 1)</code> 分を割増分として加算します。</li>
	 * <li><b>深夜手当：</b> 22:00から翌5:00までの勤務時間を1分単位で厳密にカウントし、<code>時間単価 × (深夜手当倍率 - 1)</code> 分の割増手当を加算します。退勤時間が24時を超える跨ぎ勤務にも対応しています。</li>
	 * <li><b>欠勤控除：</b> 「出勤日の1日8時間（480分）に満たない不足時間」および「平日の丸一日欠勤（有給を除く）」の合計欠勤時間を算出し、<code>時間単価</code> を乗じて控除します。</li>
	 * </ul>
	 *
	 * @param shainId  計算対象の社員ID
	 * @param workList 対象月の勤怠データリスト ({@link AttendanceData} のリスト)
	 * @return 計算された総支給額（円単位、小数点以下切り捨て）
	 */
	int getAllSalary(int shainId, List<AttendanceData> workList);
	
	/**
	 * 指定された社員の給与情報を固定長テキスト形式でCSVファイルとして生成します。
	 * <p>
	 * 出力されるファイルは「C:\workComplete\」ディレクトリ配下に、
	 * 「ログインID_年月_salary.csv」の形式で保存されます。
	 * 書き込まれるデータ（社員名、実労働日数、総支給額）は、指定されたバイト長に達するまで
	 * 半角スペースが末尾に補填されます。
	 * </p>
	 *
	 * @param yearMonth     対象年月 (例: "202606")
	 * @param actualWorkDay 実労働日数
	 * @param allSalary     総支給額
	 * @param shainName     社員名
	 * @param loginId       ログインID
	 * @return 生成された CSV ファイルの名前
	 * @throws IllegalArgumentException ファイルの新規作成、または書き込み時に {@link IOException} が発生した場合
	 */
	String createFile(String yearMonth, int actualWorkDay, int allSalary, String shainName, String loginId);
}
