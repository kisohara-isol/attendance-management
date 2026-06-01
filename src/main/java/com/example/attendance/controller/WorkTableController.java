package com.example.attendance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WorkTableController {
	
	@PostMapping(value = "/attendance/management/worktable")
	public String display(Model model) {
		return "attendance/management/worktable";
	}

}
