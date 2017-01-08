package factories;

import com.odaniait.jobframework.models.Job;
import com.odaniait.jobframework.models.Step;
import com.odaniait.jobframework.models.StepExecute;
import com.odaniait.jobframework.models.TriggerType;

import java.util.ArrayList;
import java.util.List;

public class StepFactory extends BaseFactory {
	public static Step generate() {
		Step step = new Step();
		step.setName(faker.cat().name());
		step.setExecute(StepExecute.SEQUENCE);
		step.setTriggerType(TriggerType.AUTO);

		int randomInt = faker.number().numberBetween(1, 10);
		List<Job> jobs = new ArrayList<>();
		for (int i=0 ; i<randomInt ; i++) {
			jobs.add(JobFactory.generate());
		}
		step.setJobs(jobs);

		return step;
	}
}
