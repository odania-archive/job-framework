package com.odaniait.jobframework.notifications;

import com.odaniait.jobframework.models.Build;
import factories.BuildFactory;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class ScriptNotifierTest {
	@Test
	public void testNotifierNoCommand() {
		Build build = BuildFactory.generate();

		ScriptNotifier scriptNotifier = new ScriptNotifier();
		assertTrue(!scriptNotifier.exec(build, new HashMap<>(), "This is my notifyText"));
	}

	@Test
	public void testNotifier() {
		File tmpFile = new File("/tmp/job-framework-test-script-notifier-test.txt");
		File commandFile = new File("src/test/resources/test-data/notifyTestScript.sh");

		Build build = BuildFactory.generate();
		Map<String, String> parameter = new HashMap<>();
		parameter.put("type", "script");
		parameter.put("command", commandFile.getAbsolutePath());
		String notifyText = "This is my notifyText";

		ScriptNotifier scriptNotifier = new ScriptNotifier();
		scriptNotifier.exec(build, parameter, notifyText);

		assertTrue(scriptNotifier.exec(build, parameter, notifyText));
		assertTrue(tmpFile.isFile());

		String content = readFile(tmpFile);

		for (String key : parameter.keySet()) {
			assertTrue(content.contains(key + "=" + parameter.get(key)));
		}
	}

	private String readFile(File file) {
		String result = "";
		FileReader fr;

		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String line;
			while ((line = br.readLine()) != null) {
				result += line + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
}
