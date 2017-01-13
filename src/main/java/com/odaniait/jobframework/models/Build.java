package com.odaniait.jobframework.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.odaniait.jobframework.exceptions.BuildException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode
public class Build {
	@JsonIgnore
	private static Logger logger = LoggerFactory.getLogger(Build.class);
	@JsonIgnore
	private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	private int buildNr;
	private Date startedAt = new Date();
	private Date finishedAt;
	private Long duration;
	private ResultStatus resultStatus;
	private Integer exitCode;
	private CurrentState currentState = CurrentState.RUNNING;

	private Map<String, CurrentState> stepStates = new HashMap<>();
	private Map<String, BuildJobResult> results = new HashMap<>();
	private Map<String, String> parameter = new HashMap<>();

	@JsonIgnore
	private File buildDir;

	@JsonIgnore
	private File workspaceDir;

	public void save() throws BuildException, IOException {
		logger.info("Saving Build Dir: " + buildDir);
		if (!buildDir.isDirectory() && !buildDir.mkdirs()) {
			logger.error("Error creating build directory " + buildDir);
			throw new BuildException();
		}

		File infoFile = new File(buildDir + "/info.yml");
		mapper.writeValue(infoFile, this);
	}

	public void continueStep(Step step) {
		CurrentState currentState = stepStates.get(step.getName());
		if (CurrentState.WAITING.equals(currentState)) {
			stepStates.put(step.getName(), CurrentState.TRIGGERED);
		} else {
			logger.error("Step " + step.getName() + " has wrong state! It can not be triggered! CurrentState: " + currentState);
		}
	}

	public void updateStepOutput(Step step, String output) throws IOException, BuildException {
		BuildJobResult jobResult = new BuildJobResult();
		jobResult.setOutput(output);
		jobResult.setResultStatus(ResultStatus.RUNNING);
		results.put(step.getName(), jobResult);
		stepStates.put(step.getName(), CurrentState.RUNNING);

		save();
	}

	public void setStepResult(ResultStatus resultStatus, Step step, int exitCode, String output) throws IOException, BuildException {
		BuildJobResult jobResult = results.computeIfAbsent(step.getName(), k -> new BuildJobResult());
		jobResult.setExitCode(exitCode);
		jobResult.setResultStatus(resultStatus);
		jobResult.setOutput(output);

		CurrentState stepCurrentState;
		if (ResultStatus.SUCCESS.equals(jobResult.getResultStatus())) {
			stepCurrentState = CurrentState.SUCCESS;
		} else {
			stepCurrentState = CurrentState.FAILED;
		}

		stepStates.put(step.getName(), stepCurrentState);
		save();
	}

	public void finish() throws IOException, BuildException {
		finishedAt = new Date();
		duration = finishedAt.getTime() - startedAt.getTime();

		resultStatus = ResultStatus.SUCCESS;
		exitCode = 0;
		for (BuildJobResult buildJobResult : results.values()) {
			if (!buildJobResult.getResultStatus().equals(ResultStatus.SUCCESS)) {
				resultStatus = ResultStatus.getWorseStatus(resultStatus, buildJobResult.getResultStatus());
			}

			if (exitCode < buildJobResult.getExitCode()) {
				exitCode = buildJobResult.getExitCode();
			}
		}

		if (ResultStatus.SUCCESS.equals(resultStatus)) {
			currentState = CurrentState.SUCCESS;
		} else if (ResultStatus.FAILED.equals(resultStatus)) {
			currentState = CurrentState.FAILED;
		} else if (ResultStatus.ABORTED.equals(resultStatus)) {
			currentState = CurrentState.ABORTED;
		}

		save();
	}

	@JsonIgnore
	public ResultStatus getResultStatusFor(String step) {
		BuildJobResult buildJobResult = results.get(step);

		if (buildJobResult == null) {
			return ResultStatus.NOT_STARTED;
		}

		return buildJobResult.getResultStatus();
	}
}
