package com.odaniait.jobframework.web.helper;

import com.odaniait.jobframework.pipeline.PipelineManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GeneralViewData {
	@Autowired
	private PipelineManager pipelineManager;

	@ModelAttribute
	public void addViews(Model model) {
		model.addAttribute("views", pipelineManager.getViews());
	}
}
