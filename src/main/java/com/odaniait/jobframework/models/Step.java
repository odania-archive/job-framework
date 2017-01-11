package com.odaniait.jobframework.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode
public class Step {
	private String name;

	private TriggerType triggerType = TriggerType.AUTO;
	private StepExecute execute = StepExecute.SEQUENCE;
	private Set<String> onSuccess = new HashSet<>();
	private Set<String> onError = new HashSet<>();

	private List<Job> jobs = new ArrayList<>();
}
