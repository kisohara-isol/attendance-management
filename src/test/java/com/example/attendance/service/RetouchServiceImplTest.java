package com.example.attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RetouchServiceImplTest {
	@Autowired
	private RetouchService service;
	
	//DB検索用のデータ群。これらに合致したものをアップデートする設計
	//正常データ群。対応するレコードがDBに用意されている必要がある
	private final int TARGET_SHAIN_ID = 2;
	private final LocalDate TARGET_WORK_DAY = LocalDate.of(2026, 6, 9);
	private final LocalTime TARGET_START_TIME = LocalTime.of(11, 22);
	private final LocalTime TARGET_END_TIME = LocalTime.of(23, 59);
	private final String TARGET_NOTE = "ほげほげ";
	
	//更新後データ
	private final LocalTime NEW_START_TIME = LocalTime.of(00, 00);
	private final LocalTime NEW_END_TIME = LocalTime.of(00, 00);
	private final String NEW_NOTE = "祝日";

//	//次のテストの実行時には、他のテスト並びにクラスに付与した@Transactionalをコメントアウトして実施すること
//	@MockBean
//    private DataSource dataSource; // Springのコンテキスト内のDataSourceをモックに差し替える
//    @Test
//    void test_01_DB接続失敗() throws SQLException {
//        // コネクション取得時にSQLExceptionを発生させる
//        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));
//
//        // サービスを実行すると、SpringがSQLExceptionをDataAccessExceptionにラップして投げる
//        assertThrows(DataAccessException.class, 
//        	()->service.retouchAttendance(null, null, null, 0, null, null, null, null),
//        	"DB接続失敗時にDataAccessExceptionが投げられるはず");
//    }
	
	@Test
	void test_02_正常に勤務を更新() {
		service.retouchAttendance(NEW_START_TIME, NEW_END_TIME, NEW_NOTE, TARGET_SHAIN_ID, TARGET_WORK_DAY, TARGET_START_TIME, TARGET_END_TIME, TARGET_NOTE);
		//assertTrue(true);
	}
}
