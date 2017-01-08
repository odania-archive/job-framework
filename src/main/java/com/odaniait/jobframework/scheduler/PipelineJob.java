package com.odaniait.jobframework.scheduler;

import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.pipeline.PipelineManager;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PipelineJob implements Job {
	private static Logger logger = LoggerFactory.getLogger(PipelineJob.class);

	@Autowired
	private PipelineManager pipelineManager;

	@Autowired
	private ExecutorManager executorManager;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getJobDetail().getJobDataMap();
		String pipelineId = (String) data.get("pipelineId");
		logger.info("Enqueue Pipeline " + pipelineId);
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);
		executorManager.enqueue(pipeline);
	}
}
