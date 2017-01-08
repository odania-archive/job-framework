package factories;

import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.models.Step;

import java.util.ArrayList;
import java.util.List;

public class PipelineFactory extends BaseFactory {
	public static Pipeline generate() {
		Pipeline pipeline = new Pipeline();
		pipeline.setId(faker.company().name());
		pipeline.setDescription(faker.lorem().paragraph());
		pipeline.setKeepBuilds(10);


		int randomInt = faker.number().numberBetween(3, 6);
		List<Step> steps = new ArrayList<>();
		for (int i=0 ; i<randomInt ; i++) {
			steps.add(StepFactory.generate());
		}
		pipeline.setSteps(steps);

		return pipeline;
	}
}
