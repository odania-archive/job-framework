package com.odaniait.jobframework.models;

public enum ResultStatus {
	SUCCESS,
	ABORTED,
	FAILED;


	public static ResultStatus getForExitCode(int exitCode) {
		if (exitCode == 0) {
			return SUCCESS;
		}

		if (exitCode == 1) {
			return ABORTED;
		}

		return FAILED;
	}

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
