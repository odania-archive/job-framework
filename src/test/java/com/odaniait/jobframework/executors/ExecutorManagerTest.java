package com.odaniait.jobframework.executors;

import com.odaniait.jobframework.config.JobFrameworkConfig;
import com.odaniait.jobframework.exceptions.BuildException;
import com.odaniait.jobframework.models.*;
import com.odaniait.jobframework.notifications.NotificationManager;
import com.odaniait.jobframework.pipeline.PipelineManager;
import factories.ParameterFactory;
import factories.PipelineFactory;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
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
		when(jobFrameworkConfig.getBuildStateFile()).thenAnswer(new Answer<File>() {
			@Override
			public File answer(InvocationOnMock invocation) throws Throwable {
				String randomString = new BigInteger(130, random).toString(32);
				return new File("/tmp/job-framework-test/pipelines/state_" + randomString + ".yml");
			}
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
