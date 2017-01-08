package com.odaniait.jobframework.models.params;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParamsStep {
	private String name;
	private List<JobParam> params = new ArrayList<>();
}
