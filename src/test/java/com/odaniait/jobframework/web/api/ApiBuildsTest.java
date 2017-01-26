package com.odaniait.jobframework.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odaniait.jobframework.models.Build;
import com.odaniait.jobframework.models.CurrentState;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.models.Step;
import com.odaniait.jobframework.pipeline.PipelineManager;
import factories.BuildFactory;
import factories.PipelineFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiBuildsTest {
	private static ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private PipelineManager pipelineManager;

	private MockMvc mvc;

	@Before
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void receiveNotFoundIfWrongPipeline() throws Exception {
		MockHttpServletRequestBuilder request = get("/api/pipelines/thisPipelineShouldReallyNotExist/builds/1");
		mvc.perform(request).andExpect(status().is4xxClientError());
	}

	@Test
	public void receiveNotFoundIfWrongBuild() throws Exception {
		Pipeline pipeline = PipelineFactory.generate();
		pipelineManager.getPipelines().put(pipeline.getId(), pipeline);

		MockHttpServletRequestBuilder request = get("/api/pipelines/" + pipeline.getId() + "/builds/42");
		mvc.perform(request).andExpect(status().is4xxClientError());
	}

	@Test
	public void testReceiveBuildData() throws Exception {
		Pipeline pipeline = PipelineFactory.generate();
		Build build = BuildFactory.generate();
		pipeline.getState().getBuilds().put(build.getBuildNr(), build);
		pipelineManager.getPipelines().put(pipeline.getId(), pipeline);

		MockHttpServletRequestBuilder request = get("/api/pipelines/" + pipeline.getId() + "/builds/" + build.getBuildNr());

		mvc.perform(request).andExpect(status().isOk()).
			andExpect(jsonPath("$.buildNr", is(build.getBuildNr())));
	}

	@Test
	public void testContinueBuild() throws Exception {
		Pipeline pipeline = PipelineFactory.generate(2);
		Build build = BuildFactory.generate();
		pipeline.getState().getBuilds().put(build.getBuildNr(), build);
		pipelineManager.getPipelines().put(pipeline.getId(), pipeline);

		Step step1 = pipeline.getSteps().get(1);
		Step step2 = pipeline.getSteps().get(1);

		Map<String, CurrentState> stepStates = build.getStepStates();
		stepStates.put(step1.getName(), CurrentState.SUCCESS);
		stepStates.put(step2.getName(), CurrentState.WAITING);

		MockHttpServletRequestBuilder request = post("/api/pipelines/" + pipeline.getId() + "/builds/" + build.getBuildNr() + "/" + step2.getName());
		request.contentType(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isOk()).
			andExpect(jsonPath("$.buildNr", is(build.getBuildNr())));
		assertEquals(CurrentState.SUCCESS, stepStates.get(step2.getName()));
	}
}
