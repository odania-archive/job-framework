package com.odaniait.jobframework.web;

import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.pipeline.PipelineManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Info {
	@Autowired
	private ExecutorManager executorManager;

	@Autowired
	private PipelineManager pipelineManager;

	@RequestMapping("/info")
	public String index(Model model) {
		model.addAttribute("pipelines", pipelineManager.getPipelines());
		model.addAttribute("executorManager", executorManager);

		return "info/index";
	}
}
