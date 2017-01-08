package com.odaniait.jobframework.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiTest {
	private static ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private WebApplicationContext context;

	private MockMvc mvc;

	@Before
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void requestPipeline() throws Exception {
		mvc.perform(get("/api/pipelines/pipeline-1")).andExpect(status().isOk());
	}

	@Test
	public void requestExecutePipeline() throws Exception {
		Map<String, String> data = new HashMap<>();
		data.put("p1", "test");
		data.put("p2", "dependentOption 1");
		data.put("p4", "Multiple 1,Multiple 2");

		MockHttpServletRequestBuilder postRequest = post("/pipelines/my pipeline");
		postRequest.content(mapper.writeValueAsString(data));

		mvc.perform(postRequest).andExpect(status().isOk());
	}
}
