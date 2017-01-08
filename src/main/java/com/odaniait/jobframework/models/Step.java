package com.odaniait.jobframework.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode
public class Step {
	private String name;

	private TriggerType triggerType = TriggerType.AUTO;
	private StepExecute execute = StepExecute.SEQUENCE;
	private String onSuccess;
	private String onError;

	private List<Job> jobs = new ArrayList<>();
}
