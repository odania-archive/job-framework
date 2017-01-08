package com.odaniait.jobframework.executors;

import com.odaniait.jobframework.models.*;
import factories.BuildFactory;
import factories.PipelineFactory;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class StepExecutorTest {
	@Mock
	private ExecutorManager executorManager;

	private Pipeline pipeline = new Pipeline();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		pipeline = PipelineFactory.generate();
	}

	@Test
	public void executeSimpleStep() {
		for (Step step : pipeline.getSteps()) {
			Build build = BuildFactory.generate();

			StepExecutor stepExecutor = new StepExecutor(pipeline, step, build, executorManager);
			stepExecutor.run();

			// Assert output of each job
			BuildJobResult buildJobResult = build.getResults().get(step.getName());
			for (Job job : step.getJobs()) {
				assertThat(buildJobResult.getOutput(), CoreMatchers.containsString(job.getName()));
			}
		}
	}
}
