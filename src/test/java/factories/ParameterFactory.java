package factories;

import java.util.HashMap;
import java.util.Map;

public class ParameterFactory extends BaseFactory {
	public static Map<String, String> generate() {
		Map<String, String> parameter = new HashMap<>();
		int randomInt = faker.number().numberBetween(3, 6);
		for (int i=0 ; i<randomInt ; i++) {
			parameter.put(faker.cat().name(), faker.beer().name());
		}

		return parameter;
	}
}
