package com.example.attendance.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.dto.CreateWorkRequest;
import com.example.attendance.entity.ShainData;
import com.example.attendance.repository.ShainDataMapper;

@Service
public class WorkConfirmServiceImpl implements WorkConfirmService {

	@Autowired
	private ShainDataMapper shainDataMapper;

	@Override
	@Transactional
	public void insertAttendanceData(CreateWorkRequest request, ShainData shain) throws DataAccessException {
		LocalDate workDay = request.getWorkDay();
		String note = request.getNote();

		// 1. 出勤時間の変換と秒数の固定 (:00)
		String startTime = request.getStartTime();
		if (startTime == null || startTime.isEmpty() || "休み".equals(startTime)) {
			startTime = "00:00:00";
		} else {
			// コロンや不要な文字を排除して数字だけにする (例: "09:00" -> "0900")
			startTime = startTime.replaceAll("[^0-9]", "");
			if (startTime.length() == 3) {
				startTime = "0" + startTime;
			} // 3桁補正

			// 💡 MySQLのTIME型が最も喜ぶ「HH:mm:00」形式に完全固定する
			if (startTime.length() == 4) {
				startTime = startTime.substring(0, 2) + ":" + startTime.substring(2, 4) + ":00";
			}
		}

		// 2. 退勤時間の変換と秒数の固定 (:00)
		String endTime = request.getEndTime();
		if (endTime == null || endTime.isEmpty()) {
			endTime = "00:00:00";
		} else {
			// 数字だけにクレンジング (例: "2500" や "25:00")
			endTime = endTime.replaceAll("[^0-9]", "");
			if (endTime.length() == 3) {
				endTime = "0" + endTime;
			} // 3桁補正

			// 💡 「時:分:00」にして秒数を常に00で固定
			if (endTime.length() == 4) {
				endTime = endTime.substring(0, 2) + ":" + endTime.substring(2, 4) + ":00";
			}
		}

		// 3. すでにデータが存在するかチェックしてINSERTまたはUPDATE
		int count = shainDataMapper.countAttendanceData(shain.getShainId(), workDay);

		if (count > 0) {
			shainDataMapper.updateAttendanceData(shain.getShainId(), workDay, startTime, endTime, note);
		} else {
			shainDataMapper.insertAttendanceData(shain.getShainId(), workDay, startTime, endTime, note);
		}
	}
}