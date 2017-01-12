package com.odaniait.jobframework.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.pipeline.PipelineManager;
import com.odaniait.jobframework.web.exceptions.ApiResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
		return mapper.writeValueAsString(pipelineManager.getPipelines());
	}

	@RequestMapping(value = "/{pipelineId}", method = RequestMethod.GET)
	@ResponseBody
	public String showPipeline(@PathVariable String pipelineId) throws JsonProcessingException {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);

		if (pipeline == null) {
			throw new ApiResourceNotFoundException();
		}

		return mapper.writeValueAsString(pipeline);
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
}
