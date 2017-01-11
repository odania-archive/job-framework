package factories;

import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.models.Step;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PipelineFactory extends BaseFactory {
	public static Pipeline generate() {
		int randomInt = faker.number().numberBetween(3, 6);
		return generate(randomInt);
	}

	public static Pipeline generate(int stepAmount) {
		Pipeline pipeline = new Pipeline();
		pipeline.setId(faker.company().name());
		pipeline.setDescription(faker.lorem().paragraph());
		pipeline.setKeepBuilds(10);

		List<Step> steps = new ArrayList<>();
		Set<String> stepNames = new HashSet<>();
		for (int i=0 ; i<stepAmount ; i++) {
			Step step = StepFactory.generate();
			while (stepNames.contains(step.getName())) {
				step = StepFactory.generate();
			}

			steps.add(step);
			stepNames.add(step.getName());
		}
		pipeline.setSteps(steps);

		return pipeline;
	}
}
