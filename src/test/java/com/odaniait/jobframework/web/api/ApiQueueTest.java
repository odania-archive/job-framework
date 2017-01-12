package com.odaniait.jobframework.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.models.Build;
import com.odaniait.jobframework.models.BuildState;
import com.odaniait.jobframework.models.Pipeline;
import factories.BuildFactory;
import factories.PipelineFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiQueueTest {
	private static ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ExecutorManager executorManager;

	@Autowired
	private WebApplicationContext context;

	private MockMvc mvc;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void testReceiveQueue() throws Exception {
		BuildState buildState = executorManager.getBuildState();

		mvc.perform(get("/api/queue")).andExpect(status().isOk()).
			andExpect(content().string(mapper.writeValueAsString(buildState)));
	}

	@Test
	public void removeFromQueue() throws Exception {
		Pipeline pipeline = PipelineFactory.generate();

		MockHttpServletRequestBuilder deleteRequest = delete("/api/queue/remove");
		deleteRequest.param("pipelineId", pipeline.getId());
		deleteRequest.param("idx", "0");

		executorManager.enqueue(pipeline);
		mvc.perform(deleteRequest).andExpect(status().isOk());
	}

	@Test
	public void abortBuild() throws Exception {
		Pipeline pipeline = PipelineFactory.generate();
		Build build = BuildFactory.generate();
		pipeline.getState().getBuilds().put(build.getBuildNr(), build);

		MockHttpServletRequestBuilder deleteRequest = delete("/api/queue/abortBuild");
		deleteRequest.param("pipelineId", pipeline.getId());
		deleteRequest.param("buildNr", String.valueOf(build.getBuildNr()));

		mvc.perform(deleteRequest).andExpect(status().isOk());
	}
}
