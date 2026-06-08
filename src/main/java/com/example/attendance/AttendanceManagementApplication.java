package com.example.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext; // ★これが必要
import org.springframework.context.MessageSource;     // ★これが必要

import com.example.attendance.util.LogUtil; // ★LogUtilをインポート

@SpringBootApplication
public class AttendanceManagementApplication {

	public static void main(String[] args) {
		// 1. 起動時に、Springの管理コンテナ（Context）を変数に受け取る
		ApplicationContext context = SpringApplication.run(AttendanceManagementApplication.class, args);
		
		// 2. Springが messages.properties から自動生成した MessageSource を引っ張り出す
		MessageSource messageSource = context.getBean(MessageSource.class);
		
		// 3. 引っ張り出した部品を、LogUtilに「使ってね」と渡して初期化する
		LogUtil.init(messageSource);
	}

}