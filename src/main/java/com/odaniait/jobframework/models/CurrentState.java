package com.odaniait.jobframework.models;

public enum CurrentState {
	RUNNING,
	ABORTED,
	NOT_STARTED,
	SUCCESS,
	WAITING,
	TRIGGERED,
	WAITING_ON_TRIGGER,
	FAILED
}
