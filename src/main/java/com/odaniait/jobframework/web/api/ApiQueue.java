package com.odaniait.jobframework.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.models.BuildState;
import com.odaniait.jobframework.models.QueueEntry;
import com.odaniait.jobframework.web.exceptions.ApiResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path = "/api/queue", produces = "application/json")
public class ApiQueue {
	private static ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ExecutorManager executorManager;

	@RequestMapping("")
	@ResponseBody
	public String getQueue() throws JsonProcessingException {
		BuildState buildState = executorManager.getBuildState();
		return mapper.writeValueAsString(buildState);
	}

	@RequestMapping(value = "/remove", method = RequestMethod.DELETE)
	@ResponseBody
	public String removeQueueEntry(@RequestParam("pipelineId") String pipelineId, @RequestParam("idx") int idx) throws JsonProcessingException {
		BuildState buildState = executorManager.getBuildState();

		if (buildState.getQueued().isEmpty()) {
			throw new ApiResourceNotFoundException();
		} else if (buildState.getQueued().size() < idx) {
			throw new ApiResourceNotFoundException();
		}

		QueueEntry queueEntry = buildState.getQueued().get(idx);

		if (queueEntry != null && queueEntry.getPipelineId().equals(pipelineId)) {
			buildState.getQueued().remove(idx);
		}

		return getQueue();
	}

	@RequestMapping(value = "/abortBuild", method = RequestMethod.DELETE)
	@ResponseBody
	public String abortBuild(@RequestParam("pipelineId") String pipelineId,
													 @RequestParam("buildNr") Integer buildNr) throws JsonProcessingException {
		if (pipelineId != null && buildNr != null) {
			executorManager.abortBuild(pipelineId, buildNr);
		}

		return getQueue();
	}
}
