package com.odaniait.jobframework.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Home {

	@RequestMapping("/")
	public String main() {
		return "redirect:/pipelines";
	}
}
