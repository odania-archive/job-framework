package com.odaniait.jobframework.web;

import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.pipeline.PipelineManager;
import com.odaniait.jobframework.web.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pipelines")
public class Pipelines {
	@Autowired
	private ExecutorManager executorManager;

	@Autowired
	private PipelineManager pipelineManager;

	@RequestMapping("")
	public String index(Model model) {
		model.addAttribute("pipelines", pipelineManager.getPipelines());

		return "pipelines/index";
	}

	@RequestMapping("/{pipelineId}")
	public String show(@PathVariable String pipelineId, Model model) {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);

		if (pipeline == null) {
			throw new ResourceNotFoundException();
		}

		model.addAttribute("pipeline", pipeline);
		model.addAttribute("builds", pipeline.getState().getLastBuilds());

		return "pipelines/show";
	}

	@RequestMapping("/{pipelineId}/run")
	public String run(@PathVariable String pipelineId, Model model) {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);

		if (pipeline == null) {
			throw new ResourceNotFoundException();
		}

		if (pipeline.getParamsSteps().isEmpty()) {
			executorManager.enqueue(pipeline);

			return "redirect:/pipelines/" + pipelineId;
		}

		model.addAttribute("pipeline", pipeline);
		return "pipelines/run";
	}
}
