package distributed.systems.core;

import distributed.systems.network.NodeAddress;

import java.io.Serializable;

public class MessageFactory implements Serializable {

	public final NodeAddress origin;

	public MessageFactory(NodeAddress origin) {
		this.origin = origin;
	}

	public Message createMessage() {

		return new Message(origin);
	}

	public Message createMessage(Message.Type type) {
		return new Message(origin)
				.setMessageType(type);
	}

	public Message createReply(Message message) {
		return new Message(message)
				.setOrigin(origin);
	}

	public LogMessage createLogMessage(String logMessage, LogType type) {
		LogMessage message = new LogMessage(logMessage, type);
		message.setOrigin(origin);
		return message;
	}

	public LogMessage createLogMessage(Message message) {
		return new LogMessage(message);
	}
}
