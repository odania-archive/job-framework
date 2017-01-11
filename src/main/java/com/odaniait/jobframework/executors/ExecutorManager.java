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
	private Map<Pipeline, Map<Build, List<Thread>>> buildThreads = new HashMap<>();
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

				for (String pipelineId : buildState.getCurrent().keySet()) {
					Pipeline pipeline = pipelineManager.getPipeline(pipelineId);

					for (Integer buildNr : buildState.getCurrent().get(pipelineId)) {
						Build build = pipeline.getState().getBuilds().get(buildNr);
						startBuild(pipeline, build);
					}
				}
			} catch (IOException e) {
				logger.error("Error loading build state", e);
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
		lock.lock();

		try {
			logger.info("Enqueue " + pipeline.getId());
			buildState.getQueued().add(new QueueEntry(pipeline.getId(), parameter));
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
		BuildExecutor buildExecutor = new BuildExecutor(pipeline, build, this);
		Thread thread = new Thread(buildExecutor);
		Map<Build, List<Thread>> buildListMap = buildThreads.computeIfAbsent(pipeline, k -> new HashMap<>());
		List<Thread> threads = buildListMap.computeIfAbsent(build, k -> new ArrayList<>());
		threads.add(thread);
		thread.start();

		return true;
	}

	public void finishBuild(Pipeline pipeline, Build build) {
		lock.lock();

		try {
			if (ResultStatus.SUCCESS.equals(build.getResultStatus())) {
				CurrentState lastState = pipeline.getState().getLastState();
				if (!CurrentState.SUCCESS.equals(lastState)) {
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

}
