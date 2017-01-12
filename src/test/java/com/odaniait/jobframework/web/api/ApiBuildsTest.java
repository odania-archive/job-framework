package com.odaniait.jobframework.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odaniait.jobframework.models.Build;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.pipeline.PipelineManager;
import factories.BuildFactory;
import factories.PipelineFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
