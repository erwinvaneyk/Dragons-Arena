package distributed.systems.network.logging;

import distributed.systems.core.LogMessage;
import distributed.systems.core.LogType;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class SimpleFileLogger implements Logger {

	private final InfluxLogger influx = InfluxLogger.getInstance();

	@Override
	public void log(LogMessage message) {
		logToInflux(message);
		switch (message.getLogType()) {
			case DEBUG:
				log.debug(message.toString());
				break;
			case INFO:
				log.info(message.toString());
				break;
			case WARN:
				log.warn(message.toString());
				break;
			case ERROR:
				log.error(message.toString());
				break;
		}
	}

	private void logToInflux(LogMessage message) {
		if(message.getLogType() == LogType.WARN
				|| message.getLogType() == LogType.ERROR) {
			influx.log(message);
		}
	}
}
