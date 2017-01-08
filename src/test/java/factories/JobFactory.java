package factories;

import com.odaniait.jobframework.models.Job;

public class JobFactory extends BaseFactory {
	public static Job generate() {
		Job job = new Job();
		job.setName(faker.beer().name());
		job.setScript("echo \"" + faker.book().title() + "\"");

		return job;
	}
}
