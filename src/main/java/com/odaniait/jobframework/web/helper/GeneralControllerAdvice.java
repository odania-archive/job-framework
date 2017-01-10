package com.odaniait.jobframework.web.helper;

import com.odaniait.jobframework.pipeline.PipelineManager;
import com.odaniait.jobframework.web.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GeneralControllerAdvice {
	@Autowired
	private PipelineManager pipelineManager;

	@ModelAttribute
	public void addViews(Model model) {
		model.addAttribute("views", pipelineManager.getViews());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String render404() {
		return "errors/404";
	}
}