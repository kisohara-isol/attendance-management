package com.example.attendance.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.attendance.dto.CreateWorkRequest;
import com.example.attendance.entity.ShainData;
import com.example.attendance.repository.ShainDataMapper;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class WorkConfirmServiceImplTest {

	@Autowired
	private WorkConfirmService workConfirmService;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ShainDataMapper mockShainDataMapper;

	@Test
	void test_正常系_勤務情報をDBに登録できる() {
		// 💡 【修正】引数の型を LocalTime.class から String.class に変更
		org.mockito.Mockito.doNothing().when(mockShainDataMapper).insertAttendanceData(
				org.mockito.Mockito.anyInt(),
				org.mockito.Mockito.any(LocalDate.class),
				org.mockito.Mockito.any(String.class), // ⭕ String型に対応
				org.mockito.Mockito.any(String.class), // ⭕ String型に対応
				org.mockito.Mockito.any());

		// 1. 引数の準備
		ShainData shain = new ShainData();
		shain.setLoginId("soeda123");
		shain.setShainId(1);

		CreateWorkRequest request = new CreateWorkRequest();
		request.setWorkDay(LocalDate.of(2026, 6, 10));
		request.setStartTime("0900");
		request.setEndTime("1800"); // 💡 【修正】LocalTime.of ではなく文字列に変更（例: "2500" や "4759" もテスト可能になります）

		// 2. 実行
		assertDoesNotThrow(() -> {
			workConfirmService.insertAttendanceData(request, shain);
		}, "正常なデータであれば例外（エラー）が発生せずに処理が終わるはず");
	}

	@Test
	void test_異常系_未ログインまたはセッション切れ状態でURLに直接アクセスするとログイン画面にリダイレクトされる() throws Exception {
		mockMvc.perform(get("/attendance/management/workconfirm"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/attendance/management/login"));
	}

	@Test
	void test_異常系_DB接続エラーが発生した場合にカスタム例外が発生するか() {
		// 1. 準備
		ShainData shain = new ShainData();
		shain.setShainId(1);

		CreateWorkRequest request = new CreateWorkRequest();
		request.setWorkDay(LocalDate.of(2026, 6, 10));
		request.setStartTime("0900");
		request.setEndTime("1800"); // 💡 【修正】ここも文字列に変更

		// 💡 【修正】例外発生をシミュレートするマッパーのモック引数も String.class に変更
		org.mockito.Mockito.doThrow(new org.springframework.dao.DataAccessException("DB接続エラーエラー") {
		})
				.when(mockShainDataMapper).insertAttendanceData(
						org.mockito.Mockito.anyInt(),
						org.mockito.Mockito.any(LocalDate.class),
						org.mockito.Mockito.any(String.class), // ⭕ String型に対応
						org.mockito.Mockito.any(String.class), // ⭕ String型に対応
						org.mockito.Mockito.any());

		// 2. 実行と検証
		// 💡 既存コードの設計（投げる例外が RuntimeException か、または DataAccessException のままか）に合わせてください
		assertThrows(Exception.class, () -> {
			workConfirmService.insertAttendanceData(request, shain);
		}, "DBエラーが発生した場合、Serviceは適切に例外を上層に投げるはず");
	}
}