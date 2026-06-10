package com.example.attendance.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class LoginServiceImplTest {

	@Autowired
	private LoginService loginService;

	// ローカルDBに実在するテストデータに合わせて必ず書き換えてください！
	private final String EXIST_ID = "test";       // 実際にDBにある社員ID
	private final String CORRECT_PASS = "test"; // その社員の正しいパスワード
	private final String WRONG_PASS = "wrong999";  // 嘘のパスワード

	@BeforeEach
	void setUp() {
		loginService.loginJudge(EXIST_ID, CORRECT_PASS);
	}

	@Test
	void test_01_ログイン成功() {
		int code = loginService.loginJudge(EXIST_ID, CORRECT_PASS);
		assertEquals(1, code, "正しいIDとパスワードなら1が返るはず");
		assertEquals(3, loginService.getRemainingAttempts(EXIST_ID));
	}

	@Test
	void test_02_社員が存在しない場合() {
		int code = loginService.loginJudge("99999999", CORRECT_PASS);
		assertEquals(4, code, "社員が存在しない場合は4が返るはず");
	}

	@Test
	void test_03_パスワード間違い_1回目と2回目() {
		// 1回目
		int code1 = loginService.loginJudge(EXIST_ID, WRONG_PASS);
		assertEquals(0, code1, "通常のパスワード間違い（1回目）は0が返るはず");
		assertEquals(2, loginService.getRemainingAttempts(EXIST_ID));

		// 2回目
		int code2 = loginService.loginJudge(EXIST_ID, WRONG_PASS);
		assertEquals(0, code2, "通常のパスワード間違い（2回目）は0が返るはず");
		assertEquals(1, loginService.getRemainingAttempts(EXIST_ID));
	}

	@Test
	void test_04_パスワード間違い_3回目でロックされる() {
		// 連続して3回間違えさせる
		loginService.loginJudge(EXIST_ID, WRONG_PASS);
		loginService.loginJudge(EXIST_ID, WRONG_PASS);
		int code3 = loginService.loginJudge(EXIST_ID, WRONG_PASS);
		
		assertEquals(3, code3, "3回連続で間違えたら3（新規ロック）が返るはず");
		assertEquals(0, loginService.getRemainingAttempts(EXIST_ID));
		assertEquals(1, loginService.getShainById(EXIST_ID).getStopFlg(), "DBのstopFlgが1に更新されているはず");
	}
	
	@Test
	void test_05_すでにアカウントがロックされている場合() {
		// 3回間違えさせて事前にロック状態を作る
		loginService.loginJudge(EXIST_ID, WRONG_PASS);
		loginService.loginJudge(EXIST_ID, WRONG_PASS);
		loginService.loginJudge(EXIST_ID, WRONG_PASS);

		// ロックされた状態で正しいパスワードを打つ
		int code = loginService.loginJudge(EXIST_ID, CORRECT_PASS);
		assertEquals(2, code, "すでにロックされているアカウントなら2が返るはず");
	}
}