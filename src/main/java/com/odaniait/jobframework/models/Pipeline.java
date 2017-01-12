package com.odaniait.jobframework.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.odaniait.jobframework.exceptions.BuildException;
import com.odaniait.jobframework.models.params.ParamsStep;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.util.*;

@Data
@EqualsAndHashCode(exclude = {"state"})
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

	private Set<String> tags = new HashSet<>();

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
