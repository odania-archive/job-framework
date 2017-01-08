package com.odaniait.jobframework.scheduler;

import com.odaniait.jobframework.support.AutoWiringSpringBeanJobFactory;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;
import java.util.Properties;

@Configuration
public class SchedulerConfig {
	@Bean
	public JobFactory jobFactory(ApplicationContext applicationContext)
	{
		AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		return jobFactory;
	}

	@Bean
	public Properties quartzProperties() throws IOException {
		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
		propertiesFactoryBean.afterPropertiesSet();
		return propertiesFactoryBean.getObject();
	}

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) throws IOException {
		SchedulerFactoryBean factory = new SchedulerFactoryBean();
		// this allows to update triggers in DB when updating settings in config file:
		factory.setOverwriteExistingJobs(true);
		factory.setJobFactory(jobFactory);
		factory.setQuartzProperties(quartzProperties());

		return factory;
	}
}
