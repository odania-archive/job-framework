package com.odaniait.jobframework.executors;

import com.odaniait.jobframework.config.JobFrameworkConfig;
import com.odaniait.jobframework.exceptions.BuildException;
import com.odaniait.jobframework.models.*;
import com.odaniait.jobframework.notifications.NotificationManager;
import lombok.AllArgsConstructor;
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
	private Logger logger = LoggerFactory.getLogger(ExecutorManager.class);

	@Getter
	private Map<Integer, List<Thread>> buildThreads = new HashMap<>();

	@Getter
	private Map<Pipeline, Set<Build>> current = new HashMap<>();

	@Getter
	private List<QueueEntry> queued = new ArrayList<>();
	private final Lock lock = new ReentrantLock();

	@Autowired
	private JobFrameworkConfig jobFrameworkConfig;

	@Autowired
	private NotificationManager notificationManager;

	public void enqueue(Pipeline pipeline) {
		enqueue(pipeline, new HashMap<>());
	}

	public void enqueue(Pipeline pipeline, Map<String, String> parameter) {
		logger.info("Enqueue " + pipeline.getId());
		queued.add(new QueueEntry(pipeline, parameter));
	}

	public int queueSize() {
		return queued.size();
	}

	public int activePipelines() {
		return current.size();
	}

	@Scheduled(fixedRate = 4000L)
	public Set<Build> checkQueue() throws IOException, BuildException {
		lock.lock();

		Set<Build> newBuilds = new HashSet<>();
		try {
			if (!queued.isEmpty()) {
				logger.debug("Checking Queue");

				Iterator<QueueEntry> queueIterator = queued.iterator();
				while (queueIterator.hasNext()) {
					QueueEntry queueEntry = queueIterator.next();
					Pipeline pipeline = queueEntry.getPipeline();
					if (!pipeline.getMultipleExecutions() && current.containsKey(pipeline)) {
						logger.debug("Can not start " + pipeline.getId() + " already running!");
					} else {
						logger.debug("Starting Pipeline " + pipeline.getId());
						Build build = pipeline.prepareBuild();
						Set<Build> currentBuilds = current.computeIfAbsent(pipeline, k -> new HashSet<>());
						currentBuilds.add(build);
						queueIterator.remove();
						build.setWorkspaceDir(new File(jobFrameworkConfig.getWorkspacePath() + "/" + pipeline.getId() + "/" + String.format("%08d", build.getBuildNr())));
						build.setParameter(queueEntry.getParameter());
						startSteps(pipeline, build);

						newBuilds.add(build);
					}
				}
			}
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
			Set<Build> builds = current.get(pipeline);
			builds.remove(build);
			if (builds.isEmpty()) {
				current.remove(pipeline);
			}

			pipeline.getState().finish(build);
			if (!current.containsKey(pipeline)) {
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

	@Getter
	@AllArgsConstructor
	private class QueueEntry {
		private Pipeline pipeline;
		private Map<String, String> parameter;
	}
}
