package com.odaniait.jobframework.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode
public class View {
	private String pattern;
	private Set<String> pipelines = new HashSet<>();
}
