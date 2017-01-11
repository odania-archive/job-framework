package com.odaniait.jobframework.executors;

import com.odaniait.jobframework.models.*;
import factories.BuildFactory;
import factories.PipelineFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BuildExecutorTest {

	@Mock
	private ExecutorManager executorManager;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void runSimplePipelineWithOneStep() {
		Pipeline pipeline = PipelineFactory.generate(1);
		Build build = BuildFactory.generate();

		BuildExecutor buildExecutor = new BuildExecutor(pipeline, build, executorManager);
		buildExecutor.run();

		assertEquals(1, build.getResults().size());
		assertEquals(CurrentState.SUCCESS, build.getCurrentState());
		verify(executorManager).finishBuild(pipeline, build);
	}

	@Test
	public void runSimplePipelineWithTwoStep() {
		Pipeline pipeline = PipelineFactory.generate(2);
		Build build = BuildFactory.generate();

		BuildExecutor buildExecutor = new BuildExecutor(pipeline, build, executorManager);
		buildExecutor.run();

		assertEquals(2, build.getResults().size());
		assertEquals(CurrentState.SUCCESS, build.getCurrentState());
		verify(executorManager).finishBuild(pipeline, build);
	}

	@Test
	public void runSimplePipelineWithManualTrigger() {
		Pipeline pipeline = PipelineFactory.generate(2);
		List<Step> steps = pipeline.getSteps();
		Step step = steps.get(steps.size() - 1);
		step.setTriggerType(TriggerType.MANUAL);
		Build build = BuildFactory.generate();

		BuildExecutor buildExecutor = new BuildExecutor(pipeline, build, executorManager);
		buildExecutor.run();

		assertEquals(1, build.getResults().size());
		assertEquals(CurrentState.WAITING, build.getCurrentState());
		verify(executorManager, times(0)).finishBuild(pipeline, build);
	}

	@Test
	public void continueManualTrigger() {
		Pipeline pipeline = PipelineFactory.generate(2);
		Step firstStep = pipeline.getSteps().get(0);

		Step secondStep = pipeline.getSteps().get(0);
		secondStep.setTriggerType(TriggerType.MANUAL);

		Build build = BuildFactory.generate();
		build.getStepStates().put(firstStep.getName(), CurrentState.SUCCESS);
		build.getStepStates().put(secondStep.getName(), CurrentState.WAITING);
		build.continueStep(secondStep);

		BuildExecutor buildExecutor = new BuildExecutor(pipeline, build, executorManager);
		buildExecutor.run();

		assertEquals(2, build.getResults().size());
		assertEquals(CurrentState.SUCCESS, build.getCurrentState());
		verify(executorManager).finishBuild(pipeline, build);
	}

	@Test
	public void runParallelPipeline() {
		Pipeline pipeline = PipelineFactory.generate(3);
		Build build = BuildFactory.generate();

		for (Step step : pipeline.getSteps()) {
			step.setExecute(StepExecute.PARALLEL);
		}

		BuildExecutor buildExecutor = new BuildExecutor(pipeline, build, executorManager);
		buildExecutor.run();

		assertEquals(3, build.getResults().size());
		assertEquals(CurrentState.SUCCESS, build.getCurrentState());
		verify(executorManager).finishBuild(pipeline, build);
	}

	@Test
	public void runPipelineWithOnSuccessTrigger() {
		Pipeline pipeline = PipelineFactory.generate(3);

		Step firstStep = pipeline.getSteps().get(0);
		Step secondStep = pipeline.getSteps().get(1);
		Step thirdStep = pipeline.getSteps().get(2);
		firstStep.getOnSuccess().add(secondStep.getName());
		firstStep.getOnError().add(thirdStep.getName());
		secondStep.setExecute(StepExecute.ON_TRIGGER);
		thirdStep.setExecute(StepExecute.ON_TRIGGER);
		Build build = BuildFactory.generate();

		BuildExecutor buildExecutor = new BuildExecutor(pipeline, build, executorManager);
		buildExecutor.run();

		assertEquals(2, build.getResults().size());
		assertEquals(CurrentState.SUCCESS, build.getStepStates().get(secondStep.getName()));
		assertEquals(CurrentState.WAITING_ON_TRIGGER, build.getStepStates().get(thirdStep.getName()));
		assertEquals(CurrentState.SUCCESS, build.getCurrentState());
		verify(executorManager).finishBuild(pipeline, build);
	}

	@Test
	public void runPipelineWithOnFailedTrigger() {
		Pipeline pipeline = PipelineFactory.generate(3);

		Step firstStep = pipeline.getSteps().get(0);
		Step secondStep = pipeline.getSteps().get(1);
		Step thirdStep = pipeline.getSteps().get(2);
		firstStep.getJobs().get(0).setScript("exit 1");
		firstStep.getOnSuccess().add(secondStep.getName());
		firstStep.getOnError().add(thirdStep.getName());
		secondStep.setExecute(StepExecute.ON_TRIGGER);
		thirdStep.setExecute(StepExecute.ON_TRIGGER);
		Build build = BuildFactory.generate();

		BuildExecutor buildExecutor = new BuildExecutor(pipeline, build, executorManager);
		buildExecutor.run();

		assertEquals(2, build.getResults().size());
		assertEquals(CurrentState.WAITING_ON_TRIGGER, build.getStepStates().get(secondStep.getName()));
		assertEquals(CurrentState.SUCCESS, build.getStepStates().get(thirdStep.getName()));
		assertEquals(CurrentState.ABORTED, build.getCurrentState());
		verify(executorManager).finishBuild(pipeline, build);
	}

	@Test
	public void runPipelineWithMultipleOnSuccessTrigger() {
		Pipeline pipeline = PipelineFactory.generate(4);

		Step firstStep = pipeline.getSteps().get(0);
		Step secondStep = pipeline.getSteps().get(1);
		Step thirdStep = pipeline.getSteps().get(2);
		Step fourthStep = pipeline.getSteps().get(3);
		firstStep.getOnSuccess().add(secondStep.getName());
		firstStep.getOnSuccess().add(thirdStep.getName());
		secondStep.setExecute(StepExecute.ON_TRIGGER);
		thirdStep.setExecute(StepExecute.ON_TRIGGER);
		thirdStep.getOnSuccess().add(fourthStep.getName());
		fourthStep.setExecute(StepExecute.ON_TRIGGER);
		Build build = BuildFactory.generate();

		BuildExecutor buildExecutor = new BuildExecutor(pipeline, build, executorManager);
		buildExecutor.run();

		assertEquals(4, build.getResults().size());
		assertEquals(CurrentState.SUCCESS, build.getStepStates().get(secondStep.getName()));
		assertEquals(CurrentState.SUCCESS, build.getStepStates().get(thirdStep.getName()));
		assertEquals(CurrentState.SUCCESS, build.getStepStates().get(fourthStep.getName()));
		assertEquals(CurrentState.SUCCESS, build.getCurrentState());
		verify(executorManager).finishBuild(pipeline, build);
	}
}
