package com.quickml.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
class LoginController {

	@GetMapping
	public String form(Map<String, Object> model) {
		model.put("title", "KGNCTC");
		model.put("message", "");
		return "login";
	}

}