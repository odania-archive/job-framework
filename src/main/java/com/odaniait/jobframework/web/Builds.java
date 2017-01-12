package com.odaniait.jobframework.web;

import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.models.Build;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.pipeline.PipelineManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pipelines/{pipelineId}")
public class Builds {
	@Autowired
	private ExecutorManager executorManager;

	@Autowired
	private PipelineManager pipelineManager;

	@RequestMapping("builds/{buildId}")
	public String show(@PathVariable String pipelineId, @PathVariable Integer buildId, Model model) {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);
		Build build = pipeline.getState().getBuilds().get(buildId);

		if (build == null) {
			return "redirect:/pipelines/" + pipelineId;
		}

		model.addAttribute("pipeline", pipeline);
		model.addAttribute("build", build);

		return "builds/show";
	}


	@RequestMapping("builds/latest")
	public String show(@PathVariable String pipelineId, Model model) {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);
		Build build = pipeline.getState().getLastBuild();

		if (build == null) {
			return "redirect:/pipelines/" + pipelineId;
		}

		model.addAttribute("pipeline", pipeline);
		model.addAttribute("build", build);

		return "builds/show";
	}

	@RequestMapping("builds/{buildId}/rerun")
	public String reRun(@PathVariable String pipelineId, @PathVariable Integer buildId, Model model) {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);
		Build build = pipeline.getState().getBuilds().get(buildId);

		if (build == null) {
			return "redirect:/pipelines/" + pipelineId;
		}

		executorManager.enqueue(pipeline, build.getParameter());

		return "redirect:/pipelines/" + pipelineId;
	}
}
