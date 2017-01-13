package com.odaniait.jobframework.web.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odaniait.jobframework.config.JobFrameworkConfig;
import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.pipeline.PipelineManager;
import com.odaniait.jobframework.web.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@ControllerAdvice
public class GeneralControllerAdvice {
	private final static ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private PipelineManager pipelineManager;

	@Autowired
	private ExecutorManager executorManager;

	@Autowired
	private JobFrameworkConfig jobFrameworkConfig;

	@ModelAttribute
	public void addViews(Model model) throws IOException {
		model.addAttribute("views", pipelineManager.getViews());
		model.addAttribute("buildState", executorManager.getBuildState());
		model.addAttribute("exitCodeStates", mapper.writeValueAsString(jobFrameworkConfig.getExitCodeStates()));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String render404() {
		return "errors/404";
	}
}
