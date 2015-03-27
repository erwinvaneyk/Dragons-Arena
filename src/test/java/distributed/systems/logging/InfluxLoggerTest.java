package distributed.systems.logging;

import static org.junit.Assert.assertTrue;

import distributed.systems.core.LogMessage;
import distributed.systems.core.LogType;
import distributed.systems.network.logging.InfluxLogger;
import org.junit.BeforeClass;
import org.junit.Test;

public class InfluxLoggerTest {

	@BeforeClass
	public void setup() {
		InfluxLogger.FLAG_USE_INFLUX = false;
	}

	@Test
	public void testInfluxDb() {
		InfluxLogger influx = InfluxLogger.getInstance();
		assertTrue(influx.checkConnection());
		influx.log(new LogMessage("test",LogType.DEBUG));
	}

}
