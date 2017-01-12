package com.odaniait.jobframework.executors;

import com.odaniait.jobframework.config.JobFrameworkConfig;
import com.odaniait.jobframework.exceptions.BuildException;
import com.odaniait.jobframework.models.Build;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.models.QueueEntry;
import com.odaniait.jobframework.notifications.NotificationManager;
import com.odaniait.jobframework.pipeline.PipelineManager;
import factories.PipelineFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExecutorManagerTest {
	@Mock
	private PipelineManager pipelineManager;

	@Mock
	private JobFrameworkConfig jobFrameworkConfig;

	@Mock
	private NotificationManager notificationManager;

	private SecureRandom random = new SecureRandom();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		when(jobFrameworkConfig.getWorkspacePath()).thenReturn(new File("/tmp/job-framework-test/workspace"));
		when(jobFrameworkConfig.getPipelinePath()).thenReturn("/tmp/job-framework-test/pipelines");
		when(jobFrameworkConfig.getBuildStateFile()).thenAnswer(invocation -> {
			String randomString = new BigInteger(130, random).toString(32);
			return new File("/tmp/job-framework-test/pipelines/state_" + randomString + ".yml");
		});
		new File("/tmp/job-framework-test/pipelines").mkdirs();
	}

	@Test
	public void enqueuePipeline() throws IOException, BuildException {
		Pipeline pipeline = PipelineFactory.generate();

		ExecutorManager executorManager = new ExecutorManager(jobFrameworkConfig, pipelineManager, notificationManager);
		executorManager.enqueue(pipeline);

		List<QueueEntry> queued = executorManager.getBuildState().getQueued();
		QueueEntry queueEntry = queued.get(0);

		assertEquals(1, queued.size());
		assertEquals(pipeline.getId(), queueEntry.getPipelineId());
		assertTrue(queueEntry.getParameter().isEmpty());
	}

	@Test
	public void enqueuePipelineWithParameter() throws IOException, BuildException {
		Pipeline pipeline = PipelineFactory.generate();
		Map<String, String> parameter = new HashMap<>();
		parameter.put("param1", "val1");
		parameter.put("param2", "val1,val2");

		ExecutorManager executorManager = new ExecutorManager(jobFrameworkConfig, pipelineManager, notificationManager);
		executorManager.enqueue(pipeline, parameter);

		List<QueueEntry> queued = executorManager.getBuildState().getQueued();
		QueueEntry queueEntry = queued.get(0);

		assertEquals(1, queued.size());
		assertEquals(pipeline.getId(), queueEntry.getPipelineId());
		assertEquals(parameter, queueEntry.getParameter());
	}

	@Test
	public void canNotEnqueuePipelineTwice() throws IOException, BuildException {
		Pipeline pipeline = PipelineFactory.generate();

		ExecutorManager executorManager = new ExecutorManager(jobFrameworkConfig, pipelineManager, notificationManager);
		executorManager.enqueue(pipeline);
		executorManager.enqueue(pipeline);

		List<QueueEntry> queued = executorManager.getBuildState().getQueued();
		QueueEntry queueEntry = queued.get(0);

		assertEquals(1, queued.size());
		assertEquals(pipeline.getId(), queueEntry.getPipelineId());
		assertTrue(queueEntry.getParameter().isEmpty());
	}

	@Test
	public void canNotEnqueuePipelineWithParameterTwice() throws IOException, BuildException {
		Pipeline pipeline = PipelineFactory.generate();
		Map<String, String> parameter = new HashMap<>();
		parameter.put("param1", "val1");
		parameter.put("param2", "val1,val2");

		ExecutorManager executorManager = new ExecutorManager(jobFrameworkConfig, pipelineManager, notificationManager);
		executorManager.enqueue(pipeline, parameter);
		executorManager.enqueue(pipeline, parameter);

		List<QueueEntry> queued = executorManager.getBuildState().getQueued();
		QueueEntry queueEntry = queued.get(0);

		assertEquals(1, queued.size());
		assertEquals(pipeline.getId(), queueEntry.getPipelineId());
		assertEquals(parameter, queueEntry.getParameter());
	}

	@Test
	public void canEnqueueSamePipelineWithDifferentParameters() {
		Pipeline pipeline = PipelineFactory.generate();
		Map<String, String> parameter1 = new HashMap<>();
		parameter1.put("param1", "val1");
		parameter1.put("param2", "val1,val2");

		Map<String, String> parameter2 = new HashMap<>();
		parameter2.put("param1", "val1");
		parameter2.put("param2", "val1,val2");
		parameter2.put("param3", "New Parameter");

		ExecutorManager executorManager = new ExecutorManager(jobFrameworkConfig, pipelineManager, notificationManager);
		executorManager.enqueue(pipeline);
		executorManager.enqueue(pipeline, parameter1);
		executorManager.enqueue(pipeline, parameter2);

		List<QueueEntry> queued = executorManager.getBuildState().getQueued();
		QueueEntry firstQueueEntry = queued.get(0);
		QueueEntry secondQueueEntry = queued.get(1);
		QueueEntry thirdQueueEntry = queued.get(2);

		assertEquals(3, queued.size());
		assertEquals(pipeline.getId(), firstQueueEntry.getPipelineId());
		assertTrue(firstQueueEntry.getParameter().isEmpty());
		assertEquals(pipeline.getId(), secondQueueEntry.getPipelineId());
		assertEquals(parameter1, secondQueueEntry.getParameter());
		assertEquals(pipeline.getId(), thirdQueueEntry.getPipelineId());
		assertEquals(parameter2, thirdQueueEntry.getParameter());
	}

	@Test
	public void checkQueue() throws IOException, BuildException {
		Pipeline pipeline = PipelineFactory.generate();
		when(pipelineManager.getPipeline(pipeline.getId())).thenReturn(pipeline);

		ExecutorManager executorManager = new ExecutorManager(jobFrameworkConfig, pipelineManager, notificationManager);
		executorManager.enqueue(pipeline);
		Set<Build> builds = executorManager.checkQueue();
		Build build = builds.iterator().next();

		assertEquals(1, builds.size());
		assertTrue(pipeline.getState().getBuilds().containsValue(build));
		Map<String, Set<Integer>> current = executorManager.getBuildState().getCurrent();
		assertTrue(current.get(pipeline.getId()).contains(build.getBuildNr()));
	}
}
