package com.odaniait.jobframework.web;

import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.models.BuildState;
import com.odaniait.jobframework.models.QueueEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Queue {
	@Autowired
	private ExecutorManager executorManager;

	@RequestMapping("/queue/{pipelineId}/remove/{idx}")
	public String removeQueueEntry(@PathVariable String pipelineId, @PathVariable int idx) {
		BuildState buildState = executorManager.getBuildState();

		if (buildState.getQueued().isEmpty()) {
			return "redirect:/pipelines";
		} else if (buildState.getQueued().size() < idx) {
			return "redirect:/pipelines";
		}

		QueueEntry queueEntry = buildState.getQueued().get(idx);

		if (queueEntry != null && queueEntry.getPipelineId().equals(pipelineId)) {
			buildState.getQueued().remove(idx);
		}

		return "redirect:/pipelines";
	}

	@RequestMapping("/queue/{pipelineId}/build/{buildNr}/abort")
	public String abortBuild(@PathVariable String pipelineId, @PathVariable Integer buildNr) {
		if (pipelineId != null && buildNr != null) {
			executorManager.abortBuild(pipelineId, buildNr);
		}

		return "redirect:/pipelines";
	}
}
