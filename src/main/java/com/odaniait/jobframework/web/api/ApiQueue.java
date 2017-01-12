package com.odaniait.jobframework.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.models.BuildState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path = "/api/queue", produces = "application/json")
public class ApiQueue {
	private static ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ExecutorManager executorManager;

	@RequestMapping("")
	@ResponseBody
	public String getQueue() throws JsonProcessingException {
		BuildState buildState = executorManager.getBuildState();
		return mapper.writeValueAsString(buildState);
	}
}
