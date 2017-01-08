package com.odaniait.jobframework.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class BuildJobResult {
	private int exitCode;
	private ResultStatus resultStatus;
	private String output;

	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
		resultStatus = ResultStatus.getForExitCode(exitCode);
	}
}
