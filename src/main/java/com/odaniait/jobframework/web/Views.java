package com.odaniait.jobframework.web;

import com.odaniait.jobframework.models.View;
import com.odaniait.jobframework.pipeline.PipelineManager;
import com.odaniait.jobframework.web.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Views {
	@Autowired
	private PipelineManager pipelineManager;

	@RequestMapping("/views/{viewId}")
	public String show(@PathVariable("viewId") String viewId, Model model) {
		View view = pipelineManager.getView(viewId);

		if (view == null) {
			throw new ResourceNotFoundException();
		}

		model.addAttribute("viewId", viewId);
		model.addAttribute("view", view);

		return "views/show";
	}
}
