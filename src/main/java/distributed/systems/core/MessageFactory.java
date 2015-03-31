package distributed.systems.core;

import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeState;


public class MessageFactory {
	private final NodeState origin;

	public MessageFactory(NodeState origin) {
		this.origin = origin;
	}

	public Message createMessage() {
		return new Message(origin.getAddress());
	}

	public Message createMessage(String type) {

		return new Message(origin.getAddress())
				.setMessageType(type);
	}

	public Message createReply(Message message) {
		return new Message(message)
				.setOrigin(origin.getAddress());
	}

	public LogMessage createLogMessage(Object logMessage, LogType type) {
		LogMessage message = new LogMessage(logMessage.toString(), type);
		message.setOrigin(origin.getAddress());
		return message;
	}

}
