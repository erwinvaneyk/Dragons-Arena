package distributed.systems.network.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import distributed.systems.core.LogMessage;
import distributed.systems.core.LogType;
import distributed.systems.network.logging.InfluxLogger;
import org.junit.BeforeClass;
import org.junit.Test;

public class InfluxLoggerTest {

	@Test
	public void testInfluxDb() {
		InfluxLogger influx = InfluxLogger.getInstance();
		assertEquals(influx.checkConnection(), InfluxLogger.FLAG_USE_INFLUX);
	}

}
