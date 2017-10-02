package com.odaniait.jobframework.scheduler;

import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.pipeline.PipelineManager;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Component
public class JobScheduler {
	private static Logger logger = LoggerFactory.getLogger(JobScheduler.class);

	@Autowired
	private PipelineManager pipelineManager;

	@Autowired
	private SchedulerFactoryBean scheduler;

	public void reload() throws SchedulerException, ParseException {
		scheduler.getObject().clear();
		updateJobs();
	}

	private void updateJobs() throws SchedulerException, ParseException {
		logger.info("JobScheduler :: updateJobs");
		for (Pipeline pipeline : pipelineManager.getPipelines().values()) {
			if (pipeline.getCron() == null || pipeline.getCron().isEmpty()) {
				continue;
			}

			JobDetailFactoryBean factoryBean = createJobDetail(PipelineJob.class);
			factoryBean.setName(pipeline.getId());
			factoryBean.setGroup(pipeline.getGroup());
			factoryBean.setDescription(pipeline.getDescription());
			factoryBean.afterPropertiesSet();
			factoryBean.getJobDataMap().put("pipelineId", pipeline.getId());

			CronTriggerFactoryBean cronTrigger = createCronTrigger(factoryBean.getObject(), pipeline);

			scheduler.getObject().scheduleJob(factoryBean.getObject(), cronTrigger.getObject());
		}
	}

	private static JobDetailFactoryBean createJobDetail(Class jobClass) {
		JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
		factoryBean.setJobClass(jobClass);
		// job has to be durable to be stored in DB:
		factoryBean.setDurability(true);
		return factoryBean;
	}

	// Use this method for creating cron triggers instead of simple triggers:
	private static CronTriggerFactoryBean createCronTrigger(JobDetail jobDetail, Pipeline pipeline) throws ParseException {
		CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
		factoryBean.setJobDetail(jobDetail);
		factoryBean.setGroup(pipeline.getGroup());
		factoryBean.setName(pipeline.getId());
		factoryBean.setCronExpression(pipeline.getCron());
		factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
		factoryBean.afterPropertiesSet();

		return factoryBean;
	}
}
