package com.example.attendance.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.entity.AttendanceData;

@SpringBootTest
@Transactional
class RetouchServiceImplTest {

	@Autowired
	private RetouchService retouchService;

	@Autowired
	private WorkTableService workTableService;

	// ローカルDBに実在するテストデータに合わせて必ず書き換えてください
	private final int EXIST_ID = 2; // 実際にDBにある社員のID
	private final int EXIST_YEAR = 2026; // 実際にDBにある勤務データの年
	private final int EXIST_MONTH = 1; // 実際にDBにある勤務データの月
	private final int EXIST_DAY = 1;// 実際にDBにある勤務データの日
	private final int EXIST_INDEX = 0; // 実際にDBにある勤務データの上からの順番
	private final int NEW_START_HOUR = 9; // 修正後の出勤時間(時)
	private final int NEW_START_MINUTE = 30; // 修正後の出勤時間(分)
	private final int NEW_END_HOUR = 18;// 修正後の退勤時間(時)
	private final int NEW_END_MINUTE = 30;// 修正後の退勤時間(分)
	private final String NEW_NOTE = "電車の遅延のため修正"; // 修正後の備考

	@Test
	void test_勤務表修正成功() {
		List<AttendanceData> beforeList = workTableService.getAttendanceList(EXIST_ID, EXIST_YEAR, EXIST_MONTH);
		assertFalse(beforeList.isEmpty(), "前提となるテストデータが存在するはず");
		AttendanceData originalData = beforeList.get(EXIST_INDEX);

		int shainId = originalData.getShainId();
		LocalDate workday = LocalDate.of(2026, 1, EXIST_DAY);
		LocalTime start = originalData.getStartTime();
		LocalTime end = originalData.getEndTime();
		String memo = originalData.getNote();

		LocalTime newStart = LocalTime.of(NEW_START_HOUR, NEW_START_MINUTE);
		LocalTime newEnd = LocalTime.of(NEW_END_HOUR, NEW_END_MINUTE);
		String newNote = NEW_NOTE;

		retouchService.retouchAttendance(newStart, newEnd, newNote, shainId, workday, start, end, memo);

		List<AttendanceData> afterList = workTableService.getAttendanceList(EXIST_ID, EXIST_YEAR, EXIST_MONTH);
		AttendanceData updatedData = afterList.get(EXIST_INDEX);

		assertEquals(newStart, updatedData.getStartTime(), "出勤時間が新しい時間に更新されているはず");
		assertEquals(newEnd, updatedData.getEndTime(), "退勤時間が新しい時間に更新されているはず");
		assertEquals(newNote, updatedData.getNote(), "備考が新しいメッセージに更新されているはず");

	}
}
