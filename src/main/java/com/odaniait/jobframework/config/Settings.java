package com.odaniait.jobframework.config;

import com.odaniait.jobframework.models.ExitCodeState;
import com.odaniait.jobframework.models.ResultStatus;
import com.odaniait.jobframework.models.View;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Settings {
	private Map<String, ExitCodeState> exitCodeStates = new HashMap<>();
	private Map<String, View> views = new HashMap<>();

	public Settings() {
		exitCodeStates.put("0", new ExitCodeState(ResultStatus.SUCCESS, "green", "Success"));
		exitCodeStates.put("-1", new ExitCodeState(ResultStatus.ABORTED, "darkgray", "Aborted"));
		exitCodeStates.put("default", new ExitCodeState(ResultStatus.FAILED, "red", "Failed"));
	}
}
