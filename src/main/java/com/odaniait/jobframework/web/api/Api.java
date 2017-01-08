package com.odaniait.jobframework.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.pipeline.PipelineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping(path = "/api", produces = "application/json")
public class Api {
	private Logger logger = LoggerFactory.getLogger(Api.class);
	private static ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ExecutorManager executorManager;

	@Autowired
	private PipelineManager pipelineManager;

	@RequestMapping(value = "/pipelines/{pipelineId}", method = RequestMethod.GET)
	@ResponseBody
	public String showPipeline(@PathVariable String pipelineId) throws JsonProcessingException {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);

		return mapper.writeValueAsString(pipeline);
	}

	@RequestMapping(value = "/pipelines/{pipelineId}", method = RequestMethod.POST)
	@ResponseBody
	public String startPipeline(@PathVariable String pipelineId, @RequestBody Map<String, String> data) throws JsonProcessingException {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);
		logger.info(data.toString());

		executorManager.enqueue(pipeline, data);

		return mapper.writeValueAsString(pipeline);
	}
}
