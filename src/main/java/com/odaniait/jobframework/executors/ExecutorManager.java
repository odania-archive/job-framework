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
	private Map<Pipeline, Map<Integer, Thread>> buildThreads = new HashMap<>();
	private final Lock lock = new ReentrantLock();

	private JobFrameworkConfig jobFrameworkConfig;
	private PipelineManager pipelineManager;
	private NotificationManager notificationManager;

	@Getter
	private BuildState buildState = new BuildState();

	@Autowired
	public ExecutorManager(JobFrameworkConfig jobFrameworkConfig, PipelineManager pipelineManager,
												 NotificationManager notificationManager) {
		this.jobFrameworkConfig = jobFrameworkConfig;
		this.pipelineManager = pipelineManager;
		this.notificationManager = notificationManager;

		File buildStateFile = jobFrameworkConfig.getBuildStateFile();
		if (buildStateFile.isFile()) {
			try {
				buildState = mapper.readValue(buildStateFile, BuildState.class);

				Set<String> invalidPipelines = new HashSet<>(); // Removed pipelines
				Map<String, Set<Integer>> currentBuildState = buildState.getCurrent();
				for (String pipelineId : currentBuildState.keySet()) {
					Pipeline pipeline = pipelineManager.getPipeline(pipelineId);

					if (pipeline == null) {
						invalidPipelines.add(pipelineId);
					} else {
						for (Integer buildNr : currentBuildState.get(pipelineId)) {
							Build build = pipeline.getState().getBuilds().get(buildNr);
							startBuild(pipeline, build);
						}
					}
				}

				// Remove running pipelines that have been removed from state
				for (String pipelineId : invalidPipelines) {
					currentBuildState.remove(pipelineId);
				}
			} catch (IOException e) {
				logger.error("Error loading build state", e);
			}
		}
	}

	private void saveState() {
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
		lock.lock();

		try {
			QueueEntry queueEntry = new QueueEntry(pipeline.getId(), parameter);
			if (buildState.getQueued().contains(queueEntry)) {
				logger.info("Already in queue! Not adding! Pipeline " + pipeline.getId() + " Parameter " + parameter.toString());
				return;
			}

			logger.info("Enqueue " + pipeline.getId());
			buildState.getQueued().add(queueEntry);
			saveState();
		} finally {
			lock.unlock();
		}
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

					if (pipeline == null) {
						logger.error("Could not find pipeline " + queueEntry.getPipelineId());
						queueIterator.remove();
						continue;
					}

					if (!pipeline.getMultipleExecutions() && buildState.getCurrent().containsKey(pipeline.getId())) {
						logger.debug("Can not start " + pipeline.getId() + " already running!");
					} else {
						logger.debug("Starting Pipeline " + pipeline.getId());
						Build build = pipeline.prepareBuild();
						Set<Integer> currentBuilds = buildState.getCurrent().computeIfAbsent(pipeline.getId(), k -> new HashSet<>());
						currentBuilds.add(build.getBuildNr());
						queueIterator.remove();
						build.setWorkspaceDir(new File(jobFrameworkConfig.getWorkspacePath() + "/" + pipeline.getId() + "/" + String.format("%08d", build.getBuildNr())));
						build.setParameter(queueEntry.getParameter());
						startBuild(pipeline, build);

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

	public boolean startBuild(Pipeline pipeline, Build build) {
		BuildExecutor buildExecutor = new BuildExecutor(pipeline, build, this, jobFrameworkConfig);
		Thread thread = new Thread(buildExecutor);
		Map<Integer, Thread> buildListMap = buildThreads.computeIfAbsent(pipeline, k -> new HashMap<>());
		buildListMap.put(build.getBuildNr(), thread);
		thread.start();

		return true;
	}

	void removeRunningBuild(Pipeline pipeline, Build build) {
		lock.lock();

		try {
			Map<Integer, Thread> buildListMap = buildThreads.get(pipeline);
			if (buildListMap != null) {
				buildListMap.remove(build.getBuildNr());

				if (buildListMap.isEmpty()) {
					buildThreads.remove(pipeline);
				}
			} else {
				logger.error("Expected build to be in running list! Pipeline " + pipeline.getId() + " Build " + build.getBuildNr());
			}

			Set<Integer> runningBuilds = buildState.getCurrent().get(pipeline.getId());
			if (runningBuilds != null) {
				runningBuilds.remove(build.getBuildNr());

				if (runningBuilds.isEmpty()) {
					buildState.getCurrent().remove(pipeline.getId());
				}

			} else {
				logger.error("Expected build to be in current state! Pipeline " + pipeline.getId() + " Build " + build.getBuildNr());
			}
		} finally {
			lock.unlock();
		}
	}

	void finishBuild(Pipeline pipeline, Build build) {
		removeRunningBuild(pipeline, build);
		lock.lock();

		try {
			PipelineState state = pipeline.getState();
			ResultStatus lastState = state.getResultStatus();
			state.setResultStatus(build.getResultStatus());
			state.setExitCode(build.getExitCode());
			if (ResultStatus.SUCCESS.equals(build.getResultStatus())) {
				if (!ResultStatus.SUCCESS.equals(lastState)) {
					notificationManager.notifyBackToNormal(pipeline, build);
				}
			} else {
				notificationManager.notifyFailure(pipeline, build);
			}

			try {
				cleanupBuilds(pipeline);
			} catch (IOException e) {
				logger.error("Error cleaning builds for pipeline " + pipeline.getId() + " Build " + build.getBuildNr(), e);
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

	public void abortBuild(String pipelineId, Integer buildNr) {
		lock.lock();

		try {
			Pipeline pipeline = pipelineManager.getPipeline(pipelineId);
			if (pipeline == null) {
				return;
			}

			Map<Integer, Thread> threadMap = getBuildThreads().get(pipeline);
			if (threadMap == null) {
				return;
			}

			Thread thread = threadMap.get(buildNr);
			if (thread == null) {
				return;
			}
			thread.interrupt();

		} finally {
			lock.unlock();
		}
	}
}
