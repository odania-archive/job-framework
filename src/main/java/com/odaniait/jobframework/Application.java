package com.odaniait.jobframework;

import com.odaniait.jobframework.pipeline.PipelineManager;
import com.odaniait.jobframework.scheduler.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableAutoConfiguration
@EnableAsync
@EnableScheduling
public class Application {
	private static Logger logger = LoggerFactory.getLogger(Application.class);

	@Autowired
	public Application(JobScheduler schedulerConfig, PipelineManager pipelineManager) throws Exception {
		logger.info("Init SchedulerConfig " + schedulerConfig.toString());
		pipelineManager.read();
		schedulerConfig.init();
	}


	public static void main(String[] args) throws IOException {
		logger.info("Starting Application");
		SpringApplication.run(Application.class, args);
	}
}
