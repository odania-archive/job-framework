package com.odaniait.jobframework.web;

import com.odaniait.jobframework.models.Build;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.pipeline.PipelineManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BuildsTest {
	@Autowired
	private WebApplicationContext context;

	@Autowired
	private PipelineManager pipelineManager;

	private MockMvc mvc;
	private Pipeline pipeline;

	@Before
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();

		// Find pipeline with builds
		for (Pipeline pipeline : pipelineManager.getPipelines().values()) {
			if (!pipeline.getState().getBuilds().isEmpty()) {
				this.pipeline = pipeline;
			}
		}
	}

	@Test
	public void requestShow() throws Exception {
		String buildNr = String.format("%08d", pipeline.getState().getBuilds().keySet().iterator().next());
		mvc.perform(get("/pipelines/" + pipeline.getId() + "/builds/" + buildNr)).andExpect(status().isOk());
	}

	@Test
	public void requestShowLatest() throws Exception {
		mvc.perform(get("/pipelines/" + pipeline.getId() + "/builds/latest")).andExpect(status().isOk());
	}
}
