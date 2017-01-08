package com.odaniait.jobframework.executors;

import com.odaniait.jobframework.config.JobFrameworkConfig;
import com.odaniait.jobframework.exceptions.BuildException;
import com.odaniait.jobframework.models.*;
import com.odaniait.jobframework.pipeline.PipelineManager;
import factories.ParameterFactory;
import factories.PipelineFactory;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SpringBootTest
public class ExecutorManagerTest {

	@Autowired
	private ExecutorManager executorManager;

	@Mock
	private PipelineManager pipelineManager;

	@Mock
	private JobFrameworkConfig jobFrameworkConfig;

	private Pipeline pipeline = new Pipeline();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(jobFrameworkConfig.getWorkspacePath()).thenReturn(new File("/tmp/job-framework-test/workspace"));
		when(jobFrameworkConfig.getPipelinePath()).thenReturn("/tmp/job-framework-test/pipelines");

		pipeline = PipelineFactory.generate();
		pipelineManager.getPipelines().put(pipeline.getId(), pipeline);
	}

	@Test
	public void executePipeline() throws IOException, BuildException, InterruptedException {
		PipelineState state = pipeline.getState();
		state.setPipelineDirectory(new File("/tmp/job-framework-test/builds"));
		Build lastBuild = state.getLastBuild();

		executorManager.enqueue(pipeline);
		Set<Build> builds = executorManager.checkQueue();
		Build build = builds.iterator().next();

		assertNotSame(lastBuild, build);
		build.setWorkspaceDir(new File("/tmp/job-framework-test/builds/executor-manager-test"));

		finishExecution(build);

		assertEquals(pipeline.getSteps().size(), build.getResults().size());

		for (Step step : pipeline.getSteps()) {
			BuildJobResult buildJobResult = build.getResults().get(step.getName());

			assertNotNull(buildJobResult);

			// Assert output of each job
			for (Job job : step.getJobs()) {
				assertThat(buildJobResult.getOutput(), CoreMatchers.containsString(job.getName()));
			}
		}
	}

	@Test
	public void executePipelineWithParams() throws IOException, BuildException {
		executorManager.enqueue(pipeline, ParameterFactory.generate());
		Set<Build> builds = executorManager.checkQueue();
		Build build = builds.iterator().next();
		finishExecution(build);

		assertEquals(pipeline.getSteps().size(), build.getResults().size());
	}

	private void finishExecution(Build build) {
		while (!executorManager.getBuildThreads().isEmpty()) {
			List<Thread> buildThreads = executorManager.getBuildThreads().get(build.getBuildNr());

			if (buildThreads == null) {
				break;
			}

			Iterator<Thread> threadIterator = buildThreads.iterator();
			if (threadIterator.hasNext()) {
				Thread thread = threadIterator.next();
				thread.run();
			}
		}
	}
}
