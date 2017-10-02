package com.odaniait.jobframework;

import com.odaniait.jobframework.config.JobFrameworkConfig;
import com.odaniait.jobframework.pipeline.PipelineManager;
import com.odaniait.jobframework.scheduler.JobScheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;

@Component
public class ConfigManager {
	private static Logger logger = LoggerFactory.getLogger(ConfigManager.class);

	private PipelineManager pipelineManager;
	private JobFrameworkConfig jobFrameworkConfig;
	private final JobScheduler jobScheduler;

	@Autowired
	public ConfigManager(PipelineManager pipelineManager, JobFrameworkConfig jobFrameworkConfig, JobScheduler jobScheduler) {
		this.pipelineManager = pipelineManager;
		this.jobFrameworkConfig = jobFrameworkConfig;
		this.jobScheduler = jobScheduler;
	}

	public synchronized void load() {
		try {
			jobFrameworkConfig.reloadSettings();
			pipelineManager.read();
			jobScheduler.reload();
		} catch (IOException | SchedulerException | ParseException e) {
			logger.error("Error reloading config", e);
		}
	}
}
