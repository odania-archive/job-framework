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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiBaseTest {

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
	public void testReloadConfig() throws Exception {
		MockHttpServletRequestBuilder request = post("/api/reload");

		mvc.perform(request).andExpect(status().isOk());
	}
}
