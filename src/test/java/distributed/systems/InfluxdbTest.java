package distributed.systems;

import distributed.systems.network.logging.PerformanceLogger;
import org.junit.Test;

public class InfluxdbTest {

	@Test
	public void testInfluxDb() {
		PerformanceLogger influx = PerformanceLogger.getInstance();
		influx.logLine("Test");

	}

}
