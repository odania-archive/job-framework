package factories;

import com.odaniait.jobframework.models.Build;

import java.io.File;
import java.util.Date;

public class BuildFactory extends BaseFactory {
	public static Build generate() {
		Build build = new Build();
		build.setBuildNr(faker.number().randomDigit());
		build.setStartedAt(new Date());
		build.setWorkspaceDir(new File("/tmp/job-framework-test/workspace/" + build.getBuildNr()));

		File buildDir = new File("/tmp/job-framework-test/builds/" + build.getBuildNr());
		build.setBuildDir(buildDir);

		if (!buildDir.isDirectory()) {
			buildDir.mkdirs();
		}

		return build;
	}
}
