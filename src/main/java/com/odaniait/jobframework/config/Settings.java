package com.odaniait.jobframework.config;

import com.odaniait.jobframework.models.View;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Settings {
	private Map<String, String> resultCodes = new HashMap<>();
	private Map<String, View> views = new HashMap<>();
}
