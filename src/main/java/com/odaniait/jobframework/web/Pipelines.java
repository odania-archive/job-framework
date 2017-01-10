package com.odaniait.jobframework.web;

import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.pipeline.PipelineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class Pipelines {
	private static Logger logger = LoggerFactory.getLogger(Pipelines.class);

	@Autowired
	private ExecutorManager executorManager;

	@Autowired
	private PipelineManager pipelineManager;

	@RequestMapping("/pipelines")
	public String index(Model model) {
		model.addAttribute("pipelines", pipelineManager.getPipelines());

		return "pipelines/index";
	}

	@RequestMapping("/pipelines/{pipelineId}")
	public String show(@PathVariable String pipelineId, Model model) {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);

		if (pipeline == null) {
			throw new ResourceNotFoundException();
		}

		model.addAttribute("pipeline", pipeline);
		model.addAttribute("builds", pipeline.getState().getLastBuilds());

		return "pipelines/show";
	}

	@RequestMapping("/pipelines/{pipelineId}/run")
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

	@RequestMapping("/pipelines/{pipelineId}/execute")
	public String execute(@PathVariable String pipelineId, @RequestParam("data") Map<String, String> data, Model model) {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);

		if (pipeline == null) {
			throw new ResourceNotFoundException();
		}

		// Validate parameter
		boolean isOk = true;
		logger.info(data.toString());

		if (isOk) {
			return "redirect:/pipelines/" + pipelineId;
		}

		model.addAttribute("pipeline", pipeline);
		return "pipelines/run";
	}
}
