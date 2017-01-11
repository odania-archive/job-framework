package com.odaniait.jobframework.notifications;

import com.odaniait.jobframework.models.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class ScriptNotifier implements Notifier {
	private static Logger logger = LoggerFactory.getLogger(ScriptNotifier.class);

	@Override
	public boolean exec(Build build, Map<String, String> parameter, String notificationText) {
		String command = parameter.get("command");
		if (command == null || command.isEmpty()) {
			logger.error("Error executing ScriptNotifier Command: " + parameter.get("command"));
			return false;
		}

		ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
		builder.directory(build.getBuildDir());
		Map<String, String> env = builder.environment();
		env.putAll(parameter);

		try {
			final Process process = builder.start();
			process.waitFor();

			if (process.exitValue() != 0) {
				logger.error("Error executing ScriptNotifier Command: " + parameter.get("command"));

				String output = "Output\n";
				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					output += line + "\n";
				}
				logger.error(output);
				return false;
			}
		} catch (IOException | InterruptedException e) {
			logger.error("Error executing ScriptNotifier Command: " + parameter.get("command"), e);
			return false;
		}

		return true;
	}
}
