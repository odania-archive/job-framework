package com.odaniait.jobframework.executors;

import com.odaniait.jobframework.exceptions.BuildException;
import com.odaniait.jobframework.models.*;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StepExecutor implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(StepExecutor.class);
	private SecureRandom random = new SecureRandom();

	private final Pipeline pipeline;
	private final Step step;
	private final Build build;
	private final BuildExecutor buildExecutor;

	StepExecutor(Pipeline pipeline, Step step, Build build, BuildExecutor buildExecutor) {
		this.pipeline = pipeline;
		this.step = step;
		this.build = build;
		this.buildExecutor = buildExecutor;
	}

	@Override
	public void run() {
		logger.info("StepExecutor Pipeline: " + pipeline.getId() + " Step: " + step.getName());
		build.getStepStates().put(step.getName(), CurrentState.RUNNING);

		String buildDir = build.getWorkspaceDir().getAbsolutePath();
		File stepBuildDir = new File(buildDir + "/" + step.getName().replace(" ", "-"));
		if (stepBuildDir.isDirectory()) {
			logger.info("Removing directory " + stepBuildDir + " before running Step " + step.getName());
			try {
				FileUtils.deleteDirectory(stepBuildDir);
			} catch (IOException e) {
				logger.error("Could not create step build directory " + stepBuildDir);
				try {
					build.setStepResult(step, 42, "Could not create step build directory " + stepBuildDir);
				} catch (IOException | BuildException e1) {
					logger.error("Error setting step result for Pipeline " + pipeline.getId() + " Step " + step.getName());
				}
			}
		}

		if (!stepBuildDir.mkdirs()) {
			logger.error("Could not create step build directory " + stepBuildDir);
		}

		String randomString = new BigInteger(130, random).toString(32);
		String tmpFile = stepBuildDir + "/step-" + randomString + ".sh";

		String output = "Executing Step " + step.getName() + " ==============================";
		int exitValue = -1;
		try {
			for (Job job : step.getJobs()) {
				List<String> command = new ArrayList<>();
				command.add("/bin/sh");
				command.add(tmpFile);

				output += "\n\n=== Executing Job " + job.getName() + " ===========================\n\n";

				PrintWriter writer = new PrintWriter(tmpFile, "UTF-8");
				writer.println("#!/bin/bash");
				writer.println(job.getScript());
				writer.close();

				ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
				builder.directory(stepBuildDir);
				Map<String, String> env = builder.environment();
				env.put("WORKSPACE", stepBuildDir.getAbsolutePath());

				// Set parameter
				Map<String, String> parameter = build.getParameter();
				if (parameter != null) {
					for (Map.Entry<String, String> entry : parameter.entrySet()) {
						env.put(entry.getKey(), entry.getValue());
					}
				}

				final Process process = builder.start();

				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;

				int linesProcessed = 0;
				while ((line = br.readLine()) != null) {
					output += line + "\n";

					linesProcessed++;
					if (linesProcessed >= 10) {
						build.updateStepOutput(step, output);
						linesProcessed = 0;
					}
					checkInterrupted();
				}

				logger.debug("Step " + step.getName() + " output:" + output);

				process.waitFor(); // It seems it takes a second until the process is really finished
				exitValue = process.exitValue();

				if (exitValue != 0) {
					break;
				}
			}
		} catch (IOException | InterruptedException | BuildException e) {
			logger.error("Error executing Pipeline: " + pipeline.getId() + " Step: " + step.getName(), e);
		} catch (ThreadInterruptedException e) {
			build.setCurrentState(CurrentState.ABORTED);
			return;
		}

		try {
			build.setStepResult(step, exitValue, output);
		} catch (IOException | BuildException e) {
			logger.error("Error executing Pipeline: " + pipeline.getId() + " Step: " + step.getName(), e);
		}

		buildExecutor.executeTrigger(step);
	}

	private void checkInterrupted() throws ThreadInterruptedException {
		if (Thread.interrupted()) {
			throw new ThreadInterruptedException();
		}
	}
}
