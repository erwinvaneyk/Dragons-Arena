package distributed.systems.network;

import distributed.systems.network.logging.InfluxLogger;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class CleanRunner extends BlockJUnit4ClassRunner {

	public static final int TEST_PORT_1 = 7441;
	public static final int TEST_PORT_2 = 7442;
	public static final int TEST_PORT_3 = 7443;
	public static final int TEST_PORT_4 = 7444;

	private static boolean initialized = false;

	public CleanRunner(Class<?> klass) throws InitializationError {
		super(klass);

		synchronized (CleanRunner.class) {
			if (!initialized) {
				System.out.println("Attempting to isolate testing environment...");
				InfluxLogger.FLAG_USE_INFLUX = false;
				initialized = true;
			}
		}

	}
}
