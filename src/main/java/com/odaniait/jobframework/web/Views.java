package com.odaniait.jobframework.web;

import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.models.View;
import com.odaniait.jobframework.pipeline.PipelineManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		// Find matching pipelines
		Map<String, Pipeline> pipelines = new HashMap<>();
		for (String pipelineId : view.getPipelines()) {
			pipelines.put(pipelineId, pipelineManager.getPipeline(pipelineId));
		}

		if (view.getPattern() != null) {
			Pattern pattern = Pattern.compile(view.getPattern());
			for (Map.Entry<String, Pipeline> pipelineEntry : pipelineManager.getPipelines().entrySet()) {
				Matcher matcher = pattern.matcher(pipelineEntry.getKey());
				if (matcher.find()) {
					pipelines.put(pipelineEntry.getKey(), pipelineEntry.getValue());
				}
			}
		}

		model.addAttribute("viewId", viewId);
		model.addAttribute("view", view);
		model.addAttribute("pipelines", pipelines);

		return "views/show";
	}
}
