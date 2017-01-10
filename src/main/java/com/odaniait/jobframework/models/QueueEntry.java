package com.odaniait.jobframework.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class QueueEntry {
	private String pipelineId;
	private Map<String, String> parameter;
}
