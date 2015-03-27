package distributed.systems.core;

import lombok.Data;
import lombok.NonNull;


@Data
public class LogMessage extends Message {


	public static final String MESSAGE_TYPE = "LOG";

	private final String logMessage;

	private final LogType logType;

	public LogMessage(@NonNull String logMessage, LogType type) {
		super(MESSAGE_TYPE);
		this.logMessage = logMessage;
		this.logType = type;
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
