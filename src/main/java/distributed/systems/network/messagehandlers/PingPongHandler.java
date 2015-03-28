package distributed.systems.network.messagehandlers;

import java.rmi.RemoteException;

import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;

public class PingPongHandler implements MessageHandler {

	public static final String MESSAGE_TYPE = "PING";
	public static final String RESPONSE_TYPE = "PONG";

	private final MessageFactory messageFactory;

	public PingPongHandler(MessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		return messageFactory.createMessage(RESPONSE_TYPE);
	}
}
