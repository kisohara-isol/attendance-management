package com.example.attendance.service;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.example.attendance.entity.ShainData;

/**
 * 勤務確定完了後のファイルダウンロードに関わるビジネスロジックを提供するサービス実装クラス。
 * <p>
 * 確定した給与情報および社員情報を基に、外部システム（給与計算ソフト等）と連携するための
 * MS932（Shift-JIS）エンコーディングによる固定長テキストデータの生成を担当します。
 * </p>
 *
 * @author kato (or Soeda)
 */
@Service
public class WorkComleteServiceImpl implements WorkComleteService {

	/**
	 * 送信された勤務確定データを基に、仕様で指定されたバイト数でカプセル化した固定長ファイルのバイナリデータを生成します。
	 *
	 * @param shain          ログイン中の社員データ（氏名、IDなどを含む）
	 * @param year           確定対象の年
	 * @param month          確定対象の月
	 * @param workDate       当月の実出勤日数
	 * @param salary         当月の支給総額（給料）
	 * @return MS932エンコードされた固定長1行テキストのバイト配列
	 */
	@Override
	public byte[] createFile(ShainData shain, int year, int month, int workDate, long salary) {
		
		// 1. 各項目を仕様通りの「文字列」としていったん整形
		String rawName = shain.getShainName() != null ? shain.getShainName() : "";
		String fixedDate = String.format("%02d", workDate);              // 21〜22バイト(幅2)
		String fixedSalary = String.format("%-10d", salary);               // 23〜32バイト(幅10)(%10dで右寄せ)(%-10d)-をつけつことで左寄せに代わる
		
		// 💡【修正】全角・半角が混在しても100%確実に「20バイト」でカット＆スペース詰めするバイト操作を実行
		byte[] nameBytes = convertToFixedLengthBytes(rawName, 20);
		byte[] dateBytes = fixedDate.getBytes(Charset.forName("MS932"));
		byte[] salaryBytes = fixedSalary.getBytes(Charset.forName("MS932"));
		byte[] crlfBytes = "\r\n".getBytes(Charset.forName("MS932"));
		
		// 2. 全てのバイト配列を1つに結合する（総合計：20 + 2 + 10 + 2 = 34バイト）
		byte[] oneLineBytes = new byte[nameBytes.length + dateBytes.length + salaryBytes.length + crlfBytes.length];
		
		System.arraycopy(nameBytes, 0, oneLineBytes, 0, nameBytes.length);
		System.arraycopy(dateBytes, 0, oneLineBytes, nameBytes.length, dateBytes.length);
		System.arraycopy(salaryBytes, 0, oneLineBytes, nameBytes.length + dateBytes.length, salaryBytes.length);
		System.arraycopy(crlfBytes, 0, oneLineBytes, nameBytes.length + dateBytes.length + salaryBytes.length, crlfBytes.length);
		
		return oneLineBytes;
	}

	/**
	 * 💡【全角対応バグ修正】
	 * 文字列を一度MS932のバイト配列にし、指定のバイト数ぴったりに調整した新しいバイト配列を返します。
	 * 元のデータが指定サイズを超えている場合は切り捨て、足りない場合は後ろを半角スペース（0x20）で埋めます。
	 *
	 * @param target     処理対象の文字列
	 * @param maxBytes   確定させたい正確なバイト長さ
	 * @return 長さが完全に固定されたバイト配列
	 */
	private byte[] convertToFixedLengthBytes(String target, int maxBytes) {
		byte[] srcBytes = target.getBytes(Charset.forName("MS932"));
		byte[] destBytes = new byte[maxBytes];
		
		// 初期状態として全体を半角スペース（0x20）で満たしておく
		Arrays.fill(destBytes, (byte) 0x20);
		
		// 元のデータが指定サイズより小さければ全てコピー、大きければサイズ分だけコピーして切り捨てる
		int copyLength = Math.min(srcBytes.length, maxBytes);
		System.arraycopy(srcBytes, 0, destBytes, 0, copyLength);
		
		return destBytes;
	}
}