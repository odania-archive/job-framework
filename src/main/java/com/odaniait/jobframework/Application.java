package com.odaniait.jobframework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableAutoConfiguration
@EnableAsync
@EnableScheduling
public class Application implements ApplicationListener<ContextRefreshedEvent> {
	private static Logger logger = LoggerFactory.getLogger(Application.class);

	private ConfigManager configManager;

	@Autowired
	public Application(ConfigManager configManager) {
		this.configManager = configManager;
	}

	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent ) {
		logger.info("Init Config " + configManager.toString());
		configManager.load();
	}

	public static void main(String[] args) throws IOException {
		logger.info("Starting Application");
		SpringApplication.run(Application.class, args);
	}
}
