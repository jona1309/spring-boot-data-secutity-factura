package com.bolsadeideas.springboot.app.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LocaleController {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@GetMapping("/locale")
	public String locale(HttpServletRequest request) {
		String ultimaUrl = request.getHeader("referer");
		log.info("ultimaUrl: " + ultimaUrl);
		return "redirect:" + ultimaUrl;
	}	
}
