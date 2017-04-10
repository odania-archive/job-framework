package com.odaniait.jobframework.executors;

import com.odaniait.jobframework.config.JobFrameworkConfig;
import com.odaniait.jobframework.exceptions.BuildException;
import com.odaniait.jobframework.models.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class BuildExecutor implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(BuildExecutor.class);

	private final JobFrameworkConfig jobFrameworkConfig;
	private final Pipeline pipeline;
	private final Build build;
	private final Map<String, CurrentState> stepStates;
	private final Set<CurrentState> failedStates;

	@Getter
	private final Set<Thread> threads = new HashSet<>();
	private final Set<String> triggerSteps = new HashSet<>();
	private final ExecutorManager executorManager;

	BuildExecutor(Pipeline pipeline, Build build, ExecutorManager executorManager, JobFrameworkConfig jobFrameworkConfig) {
		this.pipeline = pipeline;
		this.build = build;
		this.executorManager = executorManager;
		this.jobFrameworkConfig = jobFrameworkConfig;
		stepStates = build.getStepStates();

		failedStates = new HashSet<>();
		failedStates.add(CurrentState.NOT_STARTED);
		failedStates.add(CurrentState.RUNNING);
		failedStates.add(CurrentState.TRIGGERED);
	}

	@Override
	public void run() {
		logger.info("Started BuildExecutor for pipeline " + pipeline.getId() + " Build " + build.getBuildNr());
		try {
			exec();
		} catch (InterruptedException e) {
			logger.error("Thread interrupted", e);
			for (Thread thread : threads) {
				thread.interrupt();
			}

			build.setCurrentState(CurrentState.ABORTED);
			try {
				build.setCurrentState(CurrentState.ABORTED);
				build.setResultStatus(ResultStatus.ABORTED);
				pipeline.getState().setResultStatus(ResultStatus.ABORTED);
				pipeline.getState().setExitCode(-1);
				pipeline.getState().finish(build);
				executorManager.finishBuild(pipeline, build);
			} catch (IOException | BuildException ex) {
				logger.error("Error finishing build", ex);
			}
			return;
		}

		CurrentState finished = checkFinished();
		if (CurrentState.SUCCESS.equals(finished)) {
			logger.info("Finished Build");
			try {
				build.finish();
				pipeline.getState().finish(build);
				executorManager.finishBuild(pipeline, build);
			} catch (IOException | BuildException e) {
				logger.error("Error finishing build", e);
			}
		} else if (CurrentState.WAITING.equals(finished)) {
			build.setCurrentState(CurrentState.WAITING);
			try {
				build. save();
			} catch (BuildException | IOException e) {
				logger.error("Error saving build", e);
			}
			executorManager.removeRunningBuild(pipeline, build);
		}
	}

	private void exec() throws InterruptedException {
		for (Step step : pipeline.getSteps()) {
			executeStep(step);

			if (!StepExecute.PARALLEL.equals(step.getExecute())) {
				waitForThreads();
			}
		}

		waitForThreads();
	}

	private CurrentState checkFinished() {
		for (Step step : pipeline.getSteps()) {
			if (failedStates.contains(stepStates.get(step.getName()))) {
				return CurrentState.RUNNING;
			} else if (CurrentState.WAITING.equals(stepStates.get(step.getName()))) {
				return CurrentState.WAITING;
			}
		}

		return CurrentState.SUCCESS;
	}

	private void waitForThreads() throws InterruptedException {
		checkInterrupted();
		for (Thread thread : threads) {
			thread.join();
		}

		threads.clear();
		checkInterrupted();

		if (!triggerSteps.isEmpty()) {
			List<String> stepNames = new ArrayList<>(triggerSteps);
			triggerSteps.clear(); // Clear before starting new thread

			for (String stepName : stepNames) {
				Step step = pipeline.getStep(stepName);
				executeStep(step);
			}

			waitForThreads();
		}
	}

	private void checkInterrupted() throws InterruptedException {
		if (Thread.interrupted()) {
			for (Thread thread : threads) {
				thread.interrupt();
			}

			throw new InterruptedException();
		}
	}

	private void executeStep(Step step) throws InterruptedException {
		if (CurrentState.SUCCESS.equals(stepStates.get(step.getName()))) {
			logger.debug("Step " + step.getName() + " already finished");
			return;
		}

		// Can we run this step automatically
		if (TriggerType.AUTO.equals(step.getTriggerType()) || CurrentState.TRIGGERED.equals(stepStates.get(step.getName()))) {
			if (StepExecute.ON_TRIGGER.equals(step.getExecute()) && !CurrentState.TRIGGERED.equals(stepStates.get(step.getName()))) {
				stepStates.put(step.getName(), CurrentState.WAITING_ON_TRIGGER);
			} else {
				StepExecutor stepExecutor = new StepExecutor(pipeline, step, build, this, jobFrameworkConfig);
				Thread thread = new Thread(stepExecutor);
				threads.add(thread);
				checkInterrupted();
				thread.start();
			}
		} else {
			stepStates.put(step.getName(), CurrentState.WAITING);
		}
	}

	void executeTrigger(Step step) {
		CurrentState currentState = build.getStepStates().get(step.getName());
		if (CurrentState.SUCCESS.equals(currentState) && !step.getOnSuccess().isEmpty()) {
			for (String stepName : step.getOnSuccess()) {
				triggerSteps.add(stepName);
				stepStates.put(stepName, CurrentState.TRIGGERED);
			}
		}

		if (!CurrentState.SUCCESS.equals(currentState) && !step.getOnError().isEmpty()) {
			for (String stepName : step.getOnError()) {
				triggerSteps.add(stepName);
				stepStates.put(stepName, CurrentState.TRIGGERED);
			}
		}
	}

	void archive(Step step, File stepBuildDir) {
		if (!step.getArchive().isEmpty()) {
			File targetFolder = new File(build.getBuildDir() + "/archive/" + step.getName());
			if (!targetFolder.mkdirs()) {
				logger.error("Error creating archive folder:" + targetFolder);
			}

			for  (String archiveFileName : step.getArchive()) {
				File archiveFile = new File(stepBuildDir + "/" + archiveFileName);
				if (archiveFile.exists()) {
					File targetFile = new File(targetFolder + "/" + archiveFileName);
					logger.info("Archiving: " + archiveFile + " (Target: " + targetFile + ")");
					try {
						Files.copy(archiveFile.toPath(), targetFile.toPath());
					} catch (IOException e) {
						logger.error("Error archiving file:" + archiveFile, e);
					}
				} else {
					logger.error("File to archive does not exist: " + archiveFile);
				}
			}
		}
	}
}
