package com.odaniait.jobframework.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.odaniait.jobframework.exceptions.BuildException;
import com.odaniait.jobframework.models.params.ParamsStep;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode
public class Pipeline {

	// Config
	private String id;
	private String pipelineFolder;

	private String lastStatus;

	private String description;
	private String group;

	private String cron;

	private Integer keepBuilds;
	private Boolean multipleExecutions = false;

	private List<ParamsStep> paramsSteps = new ArrayList<>();

	private List<Step> steps = new ArrayList<>();

	private Map<String, Map<String, String>> notify = new HashMap<>();

	@JsonIgnore
	private PipelineState state = new PipelineState();

	public Build prepareBuild() throws IOException, BuildException {
		return state.prepareBuild();
	}

	public Step getStep(String name) {
		for (Step step : steps) {
			if (step.getName().equals(name)) {
				return step;
			}
		}

		return null;
	}
}
