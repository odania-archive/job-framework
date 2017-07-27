package com.odaniait.jobframework.support;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

public class HumanTime {
	public static String fromDuration( Long duration ) {
		if (duration == null) {
			return "-";
		}

		final TimeUnit scale = MILLISECONDS;

		long days = scale.toDays( duration );
		duration -= DAYS.toMillis( days );
		long hours = scale.toHours( duration );
		duration -= HOURS.toMillis( hours );
		long minutes = scale.toMinutes( duration );
		duration -= MINUTES.toMillis( minutes );
		long seconds = scale.toSeconds( duration );

		if (days > 0) {
			return String.format( "%d days, %d:%d:%d hours", days, hours, minutes, seconds);
		}

		if (hours > 0) {
			return String.format( "%d:%d:%d hours", hours, minutes, seconds);
		}

		if (minutes > 0) {
			return String.format( "%d:%d minutes", minutes, seconds);
		}

		return String.format( "%d seconds", seconds);
	}
}
