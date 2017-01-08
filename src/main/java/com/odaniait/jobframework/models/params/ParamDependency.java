package com.odaniait.jobframework.models.params;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ParamDependency {
	private String param;
	private List<String> values;
	private Map<String, String> choices; // select.id => select.name
	private boolean display = true;

	private void setValue(String value) {
		values = new ArrayList<>();
		values.add(value);
	}
}
