package com.odaniait.jobframework.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Job {

	private String name;
	private String script;
}
