package distributed.systems.core;

import lombok.Data;
import lombok.NonNull;


@Data
public class LogMessage extends Message {

	private final String message;

	private final LogType logType;

	public LogMessage(@NonNull String message, LogType type) {
		super(Type.LOG);
		this.message = message;
		this.logType = type;
	}

	public LogMessage(@NonNull String message) {
		super(Type.LOG);
		this.message = message;
		this.logType = LogType.DEBUG;
	}

	public LogMessage() {
		super(Type.LOG);
		this.message = "";
		this.logType = LogType.DEBUG;
	}

	public LogMessage(Message message) {
		super(message);
		this.message = "";
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
		if(!getMessage().isEmpty()) {
			result = getMessage() + " (" + result + ")";
		}
		return result;
	}

}
