package com.odaniait.jobframework.executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.odaniait.jobframework.config.JobFrameworkConfig;
import com.odaniait.jobframework.exceptions.BuildException;
import com.odaniait.jobframework.models.*;
import com.odaniait.jobframework.notifications.NotificationManager;
import com.odaniait.jobframework.pipeline.PipelineManager;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ExecutorManager {
	private static Logger logger = LoggerFactory.getLogger(ExecutorManager.class);
	private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	@Getter
	private Map<Integer, List<Thread>> buildThreads = new HashMap<>();
	private final Lock lock = new ReentrantLock();

	private JobFrameworkConfig jobFrameworkConfig;
	private PipelineManager pipelineManager;

	@Autowired
	private NotificationManager notificationManager;

	@Getter
	private BuildState buildState = new BuildState();

	@Autowired
	public ExecutorManager(JobFrameworkConfig jobFrameworkConfig, PipelineManager pipelineManager) throws IOException, BuildException {
		this.jobFrameworkConfig = jobFrameworkConfig;
		this.pipelineManager = pipelineManager;

		File buildStateFile = jobFrameworkConfig.getBuildStateFile();
		if (buildStateFile.isFile()) {
			buildState = mapper.readValue(buildStateFile, BuildState.class);

			for (String pipelineId: buildState.getCurrent().keySet()) {
				Pipeline pipeline = pipelineManager.getPipeline(pipelineId);

				for (Integer buildNr : buildState.getCurrent().get(pipelineId)) {
					Build build = pipeline.getState().getBuilds().get(buildNr);
					startSteps(pipeline, build);
				}
			}
		}
	}

	public void saveState() {
		try {
			File buildStateFile = jobFrameworkConfig.getBuildStateFile();
			mapper.writeValue(buildStateFile, buildState);
		} catch (IOException e) {
			logger.error("Error saving state", e);
		}
	}

	public void enqueue(Pipeline pipeline) {
		enqueue(pipeline, new HashMap<>());
	}

	public void enqueue(Pipeline pipeline, Map<String, String> parameter) {
		logger.info("Enqueue " + pipeline.getId());
		buildState.getQueued().add(new QueueEntry(pipeline.getId(), parameter));
		saveState();
	}

	@Scheduled(fixedRate = 4000L)
	public Set<Build> checkQueue() throws IOException, BuildException {
		lock.lock();

		Set<Build> newBuilds = new HashSet<>();
		try {
			if (!buildState.getQueued().isEmpty()) {
				logger.debug("Checking Queue");

				Iterator<QueueEntry> queueIterator = buildState.getQueued().iterator();
				while (queueIterator.hasNext()) {
					QueueEntry queueEntry = queueIterator.next();
					Pipeline pipeline = pipelineManager.getPipeline(queueEntry.getPipelineId());
					if (!pipeline.getMultipleExecutions() && buildState.getCurrent().containsKey(pipeline)) {
						logger.debug("Can not start " + pipeline.getId() + " already running!");
					} else {
						logger.debug("Starting Pipeline " + pipeline.getId());
						Build build = pipeline.prepareBuild();
						Set<Integer> currentBuilds = buildState.getCurrent().computeIfAbsent(pipeline.getId(), k -> new HashSet<>());
						currentBuilds.add(build.getBuildNr());
						queueIterator.remove();
						build.setWorkspaceDir(new File(jobFrameworkConfig.getWorkspacePath() + "/" + pipeline.getId() + "/" + String.format("%08d", build.getBuildNr())));
						build.setParameter(queueEntry.getParameter());
						startSteps(pipeline, build);

						newBuilds.add(build);
					}
				}
			}

			saveState();
		} finally {
			lock.unlock();
		}

		return newBuilds;
	}

	public void finishStep(Pipeline pipeline, Step step, Build build, Thread thread) throws IOException, BuildException {
		lock.lock();

		try {
			logger.info("Checking Step for Pipeline " + pipeline.getId() + " Build " + build.getBuildNr());
			List<Thread> threadList = buildThreads.get(build.getBuildNr());
			if (threadList != null) {
				threadList.remove(thread);
			}

			if (build.getStepStates().get(step.getName()).equals(CurrentState.SUCCESS)) {
				// Trigger success
				if (step.getOnSuccess() != null) {
					startStep(pipeline, pipeline.getStep(step.getOnSuccess()), build);
				}
				startSteps(pipeline, build);
			} else {
				// Trigger error
				if (step.getOnError() != null) {
					startStep(pipeline, pipeline.getStep(step.getOnError()), build);
				}

				pipeline.getState().setCurrentState(CurrentState.FAILED);
				pipeline.getState().setLastState(CurrentState.FAILED);
				notificationManager.notifyFailure(pipeline, build);
			}

			saveState();
		} finally {
			lock.unlock();
		}
	}

	private void cleanupBuilds(Pipeline pipeline) throws IOException {
		Integer keepBuilds = pipeline.getKeepBuilds();
		if (keepBuilds != null && keepBuilds > 0) {
			pipeline.getState().cleanupBuilds(keepBuilds, new File(jobFrameworkConfig.getWorkspacePath() + "/" + pipeline.getId()));
		}
	}

	private void startSteps(Pipeline pipeline, Build build) throws IOException, BuildException {
		Set<Step> triggerManualSteps = new HashSet<>();
		Set<Step> successFullSteps = new HashSet<>();
		boolean finishedAll = true;

		for (Step step : pipeline.getSteps()) {
			CurrentState stepState = build.getStepStates().get(step.getName());

			if (stepState == null) {
				if (finishedAll) {
					if (TriggerType.MANUAL.equals(step.getTriggerType())) {
						build.getStepStates().put(step.getName(), CurrentState.WAITING);
						triggerManualSteps.add(step);
					} else if (!step.getExecute().equals(StepExecute.ON_TRIGGER)) {
						startStep(pipeline, step, build);
					}

					if (!step.getExecute().equals(StepExecute.PARALLEL)) {
						break;
					}
				}
			} else if (stepState.equals(CurrentState.SUCCESS)) {
				successFullSteps.add(step);
			} else if (stepState.equals(CurrentState.WAITING)) {
				if (!step.getExecute().equals(StepExecute.PARALLEL)) {
					break;
				}

				finishedAll = false;
			} else {
				break;
			}
		}

		if (successFullSteps.size() == pipeline.getSteps().size()) {
			logger.info("Finished Pipeline " + pipeline.getId());
			CurrentState lastState = pipeline.getState().getLastState();
			pipeline.getState().setLastState(CurrentState.SUCCESS);
			build.setCurrentState(CurrentState.SUCCESS);

			buildThreads.remove(build.getBuildNr());
			Set<Integer> builds = buildState.getCurrent().get(pipeline.getId());
			builds.remove(build.getBuildNr());
			if (builds.isEmpty()) {
				buildState.getCurrent().remove(pipeline.getId());
			}

			pipeline.getState().finish(build);
			if (!buildState.getCurrent().containsKey(pipeline.getId())) {
				pipeline.getState().setCurrentState(build.getCurrentState());
				pipeline.getState().setLastDuration(build.getDuration());
			}
			pipeline.getState().save();

			if (!CurrentState.SUCCESS.equals(lastState)) {
				notificationManager.notifyBackToNormal(pipeline, build);
			}

			cleanupBuilds(pipeline);
		} else if (!triggerManualSteps.isEmpty()) {
			build.setCurrentState(CurrentState.WAITING);
		}
	}

	private void startStep(Pipeline pipeline, Step step, Build build) {
		StepExecutor stepExecutor = new StepExecutor(pipeline, step, build, this);
		Thread thread = new Thread(stepExecutor);
		stepExecutor.setThread(thread);

		List<Thread> threadList = this.buildThreads.computeIfAbsent(build.getBuildNr(), k -> new ArrayList<>());
		threadList.add(thread);
		thread.start();
	}


}
