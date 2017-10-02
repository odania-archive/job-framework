package com.odaniait.jobframework.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odaniait.jobframework.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(path = "/api", produces = "application/json")
public class ApiBase {
	private static ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ConfigManager configManager;

	@RequestMapping(value = "/reload", method = RequestMethod.POST)
	@ResponseBody
	public String reloadConfigs() throws JsonProcessingException {
		configManager.load();

		Map<String, String> response = new HashMap<>();
		response.put("status", "ok");

		return mapper.writeValueAsString(response);
	}
}
