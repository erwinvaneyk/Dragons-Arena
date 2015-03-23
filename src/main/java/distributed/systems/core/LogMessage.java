package distributed.systems.core;

import lombok.Data;
import lombok.NonNull;


@Data
public class LogMessage extends Message {

	private final String logMessage;

	private final LogType logType;

	public LogMessage(@NonNull String logMessage, LogType type) {
		super(Type.LOG);
		this.logMessage = logMessage;
		this.logType = type;
	}

	LogMessage(Message logMessage) {
		super(logMessage);
		this.logMessage = "";
		this.logType = LogType.DEBUG;
	}

    public LogType getLogType() {
        return logType;
    }

	@Override
	public String toString() {
		String result = "send: " + getTimestamp();
		if(!getContent().isEmpty()) {
			result += " contents=" + getContent().toString();
		}
		if(!getLogMessage().isEmpty()) {
			result = getLogMessage() + " (" + result + ")";
		}
		return "[" + this.getOrigin() + "] " + result;
	}

}
