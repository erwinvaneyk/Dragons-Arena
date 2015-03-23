package distributed.systems.network.logging;

import distributed.systems.core.LogMessage;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class SimpleFileLogger implements Logger {

	@Override
	public void log(LogMessage message) {
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
}
