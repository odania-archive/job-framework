package com.odaniait.jobframework.models;

import lombok.Data;

import java.util.*;

@Data
public class BuildState {
	private Map<String, Set<Integer>> current = new HashMap<>();
	private List<QueueEntry> queued = new ArrayList<>();
}
