package com.example.attendance.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.entity.AttendanceData;

@SpringBootTest
@Transactional
class WorkTableServiceImplTest {

	@Autowired
	private WorkTableService workTableService;

	// ローカルDBに実在するテストデータに合わせて必ず書き換えてください
	private final int EXIST_ID = 2; // 実際にDBにある社員のID
	private final int EXIST_YEAR = 2026; // 実際にDBにある勤務データの年
	private final int EXIST_MONTH = 1; // 実際にDBにある勤務データの月
	private final int EMPTY_YEAR = 2000; // 実際にDBにある勤務データが無い年
	private final int EXIST_INDEX_01 = 0; // 実際にDBにある勤務データの上からの順番(通常時)
	private final int EXISR_INDEX_02 = 1; // 実際にDBにある勤務データの上からの順番(残業時)
	private final int EXIST_INDEX_03 = 3; // 実際にDBにある勤務データの上からの順番(休み)
	private final int EXIST_INDEX_04 = 4; // 実際にDBにある勤務データの上からの順番(休みではない時)

	@Test
	void test_01_勤務表照会成功_通常時() {
		List<AttendanceData> list = workTableService.getAttendanceList(EXIST_ID, EXIST_YEAR, EXIST_MONTH);
		assertFalse(list.isEmpty(), "テストデータが1件以上返却されるはず");
		AttendanceData firstData = list.get(EXIST_INDEX_01);
		assertEquals(EXIST_ID, firstData.getShainId());
		assertNotNull(firstData.getWorkDay(), "日付が yyyy/MM/dd 形式で入っているはず");
		assertNotNull(firstData.getDayOfWeek(), "曜日が（月、火など）入っているはず");
		assertNotNull(firstData.getNote(), "備考が入っているはず");
		assertNotNull(firstData.getMinutes(), "勤務時間が入っているはず");
		assertNotNull(firstData.getStartTime(), "出勤時間が入っているはず");
		assertNotNull(firstData.getEndTime(), "退勤時間が入っているはず");
		assertNull(firstData.getOverTime(), "残業時間が入っていないはず");
		assertFalse(firstData.isBreakDay(), "休みになっていないはず");
	}

	@Test
	void test_02_勤務表照会成功_残業時() {
		List<AttendanceData> list = workTableService.getAttendanceList(EXIST_ID, EXIST_YEAR, EXIST_MONTH);
		assertFalse(list.isEmpty(), "テストデータが1件以上返却されるはず");
		AttendanceData firstData = list.get(EXISR_INDEX_02);
		assertEquals(EXIST_ID, firstData.getShainId());
		assertNotNull(firstData.getWorkDay(), "日付が yyyy/MM/dd 形式で入っているはず");
		assertNotNull(firstData.getDayOfWeek(), "曜日が（月、火など）入っているはず");
		assertNotNull(firstData.getMinutes(), "勤務時間が入っているはず");
		assertNotNull(firstData.getStartTime(), "出勤時間が入っているはず");
		assertNotNull(firstData.getEndTime(), "退勤時間が入っているはず");
		assertNotNull(firstData.getOverTime(), "残業時間が入っているはず");
		assertFalse(firstData.isBreakDay(), "休みになっていないはず");
	}

	@Test
	void test_03_勤務表照会成功_休み() {
		List<AttendanceData> list = workTableService.getAttendanceList(EXIST_ID, EXIST_YEAR, EXIST_MONTH);
		assertFalse(list.isEmpty(), "テストデータが1件以上返却されるはず");
		AttendanceData firstData = list.get(EXIST_INDEX_03);
		assertEquals(EXIST_ID, firstData.getShainId());
		assertNotNull(firstData.getWorkDay(), "日付が yyyy/MM/dd 形式で入っているはず");
		assertNotNull(firstData.getDayOfWeek(), "曜日が（月、火など）入っているはず");
		assertNotNull(firstData.getMinutes(), "勤務時間が入っているはず");
		assertNotNull(firstData.getStartTime(), "出勤時間が入っているはず");
		assertNotNull(firstData.getEndTime(), "退勤時間が入っているはず");
		assertNull(firstData.getOverTime(), "残業時間が入っていないはず");
		assertTrue(firstData.isBreakDay(), "休みになっているはず");
	}

	@Test
	void test_04_勤務表照会成功_休みではない() {
		List<AttendanceData> list = workTableService.getAttendanceList(EXIST_ID, EXIST_YEAR, EXIST_MONTH);
		assertFalse(list.isEmpty(), "テストデータが1件以上返却されるはず");
		AttendanceData firstData = list.get(EXIST_INDEX_04);
		assertEquals(EXIST_ID, firstData.getShainId());
		assertNotNull(firstData.getWorkDay(), "日付が yyyy/MM/dd 形式で入っているはず");
		assertNotNull(firstData.getDayOfWeek(), "曜日が（月、火など）入っているはず");
		assertNotNull(firstData.getMinutes(), "勤務時間が入っているはず");
		assertNotNull(firstData.getStartTime(), "出勤時間が入っているはず");
		assertNotNull(firstData.getEndTime(), "退勤時間が入っているはず");
		assertNull(firstData.getOverTime(), "残業時間が入っていないはず");
		assertFalse(firstData.isBreakDay(), "休みになっていないはず");
	}

	@Test
	void test_0_勤務表照会なし() {
		List<AttendanceData> list = workTableService.getAttendanceList(EXIST_ID, EMPTY_YEAR, EXIST_MONTH);
		assertTrue(list.isEmpty(), "テストデータが1件も無いはず");
	}
}
