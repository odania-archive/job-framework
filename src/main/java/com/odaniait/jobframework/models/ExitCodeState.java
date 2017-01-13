package com.odaniait.jobframework.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExitCodeState {
	private ResultStatus resultStatus;
	private String color;
	private String description;
}
