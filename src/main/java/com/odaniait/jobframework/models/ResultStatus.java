package com.odaniait.jobframework.models;

public enum ResultStatus {
	SUCCESS,
	ABORTED,
	FAILED,
	NOT_STARTED,
	RUNNING;

	private static int getRate(ResultStatus resultStatus) {
		if (resultStatus.equals(SUCCESS)) {
			return 0;
		}
		if (resultStatus.equals(ABORTED)) {
			return 1;
		}

		return 2;
	}

	public static ResultStatus getWorseStatus(ResultStatus resultStatus, ResultStatus resultStatus1) {
		if (getRate(resultStatus) < getRate(resultStatus1)) {
			return resultStatus1;
		}

		return resultStatus;
	}
}
