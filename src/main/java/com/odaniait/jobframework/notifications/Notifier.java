package com.odaniait.jobframework.notifications;

import com.odaniait.jobframework.models.Build;

import java.util.Map;

public interface Notifier {
	boolean exec(Build build, Map<String, String> parameter, String notificationText);
}
