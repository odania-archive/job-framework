package com.odaniait.jobframework.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.models.Build;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.models.PipelineState;
import com.odaniait.jobframework.pipeline.PipelineManager;
import com.odaniait.jobframework.web.exceptions.ApiResourceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping(path = "/api/pipelines", produces = "application/json")
public class ApiPipelines {
	private static ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ExecutorManager executorManager;

	@Autowired
	private PipelineManager pipelineManager;

	@RequestMapping("")
	@ResponseBody
	public String showPipelines() throws JsonProcessingException {
		Map<String, PipelineStateResponse> response = new HashMap<>();
		for (Pipeline pipeline : pipelineManager.getPipelines().values()) {
			response.put(pipeline.getId(), new PipelineStateResponse(pipeline, pipeline.getState(), null));
		}

		return mapper.writeValueAsString(response);
	}

	@RequestMapping(value = "/{pipelineId}", method = RequestMethod.GET)
	@ResponseBody
	public String showPipeline(@PathVariable String pipelineId) throws JsonProcessingException {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);

		if (pipeline == null) {
			throw new ApiResourceNotFoundException();
		}

		Map<Integer, Build> builds = pipeline.getState().getBuilds();
		List<Build> sortedBuilds = new ArrayList<>();
		List<Integer> buildKeys = new ArrayList<>(builds.keySet());
		Collections.sort(buildKeys);
		Collections.reverse(buildKeys);

		for (Integer buildNr : buildKeys) {
			sortedBuilds.add(builds.get(buildNr));
		}

		return mapper.writeValueAsString(new PipelineStateResponse(pipeline, pipeline.getState(), sortedBuilds));
	}

	@RequestMapping(value = "/{pipelineId}", method = RequestMethod.POST)
	@ResponseBody
	public String startPipeline(@PathVariable String pipelineId, @RequestBody Map<String, String> data) throws JsonProcessingException {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);

		if (pipeline == null) {
			throw new ApiResourceNotFoundException();
		}

		executorManager.enqueue(pipeline, data);

		return mapper.writeValueAsString(pipeline);
	}

	@Data
	@AllArgsConstructor
	private class PipelineStateResponse {
		private Pipeline pipeline;
		private PipelineState state;
		private List<Build> builds = new ArrayList<>();
	}
}
