package com.odaniait.jobframework.web.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odaniait.jobframework.models.Build;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.pipeline.PipelineManager;
import com.odaniait.jobframework.web.exceptions.ApiResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path = "/api/pipelines/{pipelineId}/builds", produces = "application/json")
public class ApiBuilds {
	private static ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private PipelineManager pipelineManager;

	@RequestMapping(value = "/{buildNr}", method = RequestMethod.GET)
	@ResponseBody
	public String showBuild(@PathVariable String pipelineId, @PathVariable Integer buildNr) throws JsonProcessingException {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);

		if (pipeline == null) {
			throw new ApiResourceNotFoundException();
		}

		Build build = pipeline.getState().getBuilds().get(buildNr);

		if (build == null) {
			throw new ApiResourceNotFoundException();
		}

		return mapper.writeValueAsString(build);
	}
}
