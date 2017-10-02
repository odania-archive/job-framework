package com.odaniait.jobframework.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.odaniait.jobframework.models.ExitCodeState;
import com.odaniait.jobframework.models.ResultStatus;
import com.odaniait.jobframework.pipeline.PipelineManager;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component
public class JobFrameworkConfig {
	private static Logger logger = LoggerFactory.getLogger(PipelineManager.class);
	private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	@Value("${jobframework.baseDirectory}")
	@Getter
	private String baseDirectory;

	private Settings settings;

	public String getPipelinePath() {
		return baseDirectory + "/pipelines";
	}

	public File getWorkspacePath() {
		return new File(baseDirectory + "/workspace");
	}

	public Settings getSettings() throws IOException {
		if (settings == null) {
			logger.info("Reading configuration");
			File settingsFile = new File(baseDirectory + "/settings.yml");

			if (settingsFile.isFile()) {
				settings = mapper.readValue(settingsFile, Settings.class);
			} else {
				settings = new Settings();
			}
		}

		return settings;
	}

	public Settings reloadSettings() throws IOException {
		this.settings = null;
		return getSettings();
	}

	public File getBuildStateFile() {
		return new File(baseDirectory + "/buildState.yml");
	}

	public Map<String, ExitCodeState> getExitCodeStates() throws IOException {
		return getSettings().getExitCodeStates();
	}

	public ResultStatus getForExitCode(int exitCode) throws IOException {
		Settings settings = getSettings();
		if (settings == null) {
			return getDefaultForExitCode(exitCode);
		}

		Map<String, ExitCodeState> exitCodeStates = settings.getExitCodeStates();

		// No values are defined! Use defaults
		if (exitCodeStates.isEmpty()) {
			return getDefaultForExitCode(exitCode);
		}

		ExitCodeState exitCodeState = exitCodeStates.get(String.valueOf(exitCode));

		if (exitCodeState != null) {
			return exitCodeState.getResultStatus();
		}

		exitCodeState = exitCodeStates.get("default");
		if (exitCodeState != null) {
			return exitCodeState.getResultStatus();
		}

		return ResultStatus.FAILED;
	}

	private ResultStatus getDefaultForExitCode(int exitCode) {
		if (exitCode == 0) {
			return ResultStatus.SUCCESS;
		}

		return ResultStatus.FAILED;
	}
}
