package distributed.systems.network.logging;

import distributed.systems.core.LogMessage;

public interface Logger {

	public void log(LogMessage message);

	public static Logger getDefault() {
		return new SimpleFileLogger();
	}
}
