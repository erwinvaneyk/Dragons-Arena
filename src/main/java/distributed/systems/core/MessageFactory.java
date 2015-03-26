package distributed.systems.core;

import distributed.systems.network.NodeAddress;

public class MessageFactory {

	private final NodeAddress origin;

	public MessageFactory(NodeAddress origin) {
		this.origin = origin;
	}

	public Message createMessage() {
		return new Message(origin);
	}

	public Message createMessage(String type) {
		return new Message(origin)
				.setMessageType(type);
	}

	public Message createReply(Message message) {
		return new Message(message)
				.setOrigin(origin);
	}

	public LogMessage createLogMessage(Object logMessage, LogType type) {
		LogMessage message = new LogMessage(logMessage.toString(), type);
		message.setOrigin(origin);
		return message;
	}
}
