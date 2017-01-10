package com.odaniait.jobframework.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.odaniait.jobframework.config.JobFrameworkConfig;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.models.PipelineState;
import com.odaniait.jobframework.models.View;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class PipelineManager {
	private static Logger logger = LoggerFactory.getLogger(PipelineManager.class);
	private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	@Getter
	private Map<String, Pipeline> pipelines = new HashMap<>();

	@Getter
	private final Map<String, View> views;
	private final JobFrameworkConfig jobFrameworkConfig;

	@Autowired
	public PipelineManager(JobFrameworkConfig jobFrameworkConfig) throws IOException {
		this.jobFrameworkConfig = jobFrameworkConfig;
		this.views = jobFrameworkConfig.getSettings().getViews();
	}

	public void read() throws IOException {
		readPipelines();
	}

	public void readPipelines() throws IOException {
		File pipelinePath = new File(jobFrameworkConfig.getPipelinePath());
		File[] fList = pipelinePath.listFiles();

		if (fList != null) {
			for (File file : fList) {
				if (file.isDirectory()) {
					logger.info("Reading pipeline: " + file.toString());
					readPipeline(file);
				}
			}
		}
	}

	private void readPipeline(File path) throws IOException {
		File pipelineConfigFile = new File(path.getAbsolutePath() + "/config.yml");

		if (pipelineConfigFile.isFile()) {
			String pipelineId = path.getName();
			Pipeline pipeline = mapper.readValue(pipelineConfigFile, Pipeline.class);
			pipeline.setId(pipelineId);
			pipeline.setPipelineFolder(path.getAbsolutePath());

			// Read State
			File pipelineDirectory = new File(path.getAbsolutePath());
			File stateFile = new File(pipelineDirectory + "/state.yml");
			if (stateFile.isFile()) {
				PipelineState pipelineState = mapper.readValue(stateFile, PipelineState.class);
				pipeline.setState(pipelineState);
			}
			pipeline.getState().initialize(pipelineDirectory, jobFrameworkConfig.getWorkspacePath() + "/" + pipeline.getId());

			pipelines.put(pipelineId, pipeline);
		} else {
			logger.error("Could not read pipeline config in: " + path);
		}
	}

	public Pipeline getPipeline(String pipelineId) {
		return pipelines.get(pipelineId);
	}

	public View getView(String viewId) {
		return views.get(viewId);
	}
}
