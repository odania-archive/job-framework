package com.odaniait.jobframework.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odaniait.jobframework.executors.ExecutorManager;
import com.odaniait.jobframework.models.BuildState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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
}
