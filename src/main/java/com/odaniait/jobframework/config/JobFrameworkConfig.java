package com.odaniait.jobframework.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.odaniait.jobframework.pipeline.PipelineManager;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

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

	public File getBuildStateFile() {
		return new File(baseDirectory + "/buildState.yml");
	}
}
