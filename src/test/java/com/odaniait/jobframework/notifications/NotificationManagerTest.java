package com.odaniait.jobframework.notifications;

import com.odaniait.jobframework.models.Build;
import com.odaniait.jobframework.models.Pipeline;
import factories.BuildFactory;
import factories.PipelineFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class NotificationManagerTest {
	@Mock
	private EmailNotifier emailNotifier;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testNotifyOnFailure() {
		Map<String, String> parameter = new HashMap<>();
		parameter.put("type", "email");
		parameter.put("emails", "test@example.com");

		Pipeline pipeline = PipelineFactory.generate(1);
		Build build = BuildFactory.generate();
		pipeline.getNotify().put("testEmailNotify", parameter);

		NotificationManager notificationManager = new NotificationManager(emailNotifier);
		notificationManager.notifyFailure(pipeline, build);

		ArgumentCaptor<Build> buildCaptor = ArgumentCaptor.forClass(Build.class);
		ArgumentCaptor<String> notifyTextCaptor = ArgumentCaptor.forClass(String.class);
		verify(emailNotifier).exec(buildCaptor.capture(), any(HashMap.class), notifyTextCaptor.capture());

		assertEquals(build, buildCaptor.getValue());
		assertTrue(notifyTextCaptor.getValue().startsWith("[FAILURE]"));
		assertTrue(notifyTextCaptor.getValue().contains("Pipeline " + pipeline.getId()));
		assertTrue(notifyTextCaptor.getValue().contains("Build: " + build.getBuildNr()));
	}

	@Test
	public void testNotifyBackToNormal() {
		Map<String, String> parameter = new HashMap<>();
		parameter.put("type", "email");
		parameter.put("emails", "test@example.com");

		Pipeline pipeline = PipelineFactory.generate(1);
		Build build = BuildFactory.generate();
		pipeline.getNotify().put("testEmailNotify", parameter);

		NotificationManager notificationManager = new NotificationManager(emailNotifier);
		notificationManager.notifyBackToNormal(pipeline, build);

		ArgumentCaptor<Build> buildCaptor = ArgumentCaptor.forClass(Build.class);
		ArgumentCaptor<String> notifyTextCaptor = ArgumentCaptor.forClass(String.class);
		verify(emailNotifier).exec(buildCaptor.capture(), any(HashMap.class), notifyTextCaptor.capture());

		assertEquals(build, buildCaptor.getValue());
		assertTrue(notifyTextCaptor.getValue().startsWith("[BACK-TO-NORMAL]"));
		assertTrue(notifyTextCaptor.getValue().contains("Pipeline " + pipeline.getId()));
		assertTrue(notifyTextCaptor.getValue().contains("Build: " + build.getBuildNr()));
	}
}
