package com.example.attendance.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @author Soeda
 * */

import lombok.Data;

@Data //getter,setterが自動敵に使えるようになる

public class LoginRequest {

	/** ログインID */
	@NotBlank(message = "ログインIDを入力してください")
	private String loginId;

	/** パスワード */
	@NotBlank(message = "パスワードを入力してください")
	private String password;

	public LoginRequest() {

	}
}
