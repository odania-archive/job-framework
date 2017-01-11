package com.odaniait.jobframework.models.params;

import lombok.Data;

import java.util.*;

@Data
public class JobParam {
	private String name;
	private JobParamType paramType;
	private String hint;
	private List<ParamDependency> dependencies = new ArrayList<>();

	public String getFieldName() {
		return "data[" + name + "]";
	}
}
