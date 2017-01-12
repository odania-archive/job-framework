package com.odaniait.jobframework.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class QueueEntry {
	private String pipelineId;
	private Map<String, String> parameter;
}
