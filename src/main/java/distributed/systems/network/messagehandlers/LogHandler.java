package distributed.systems.network.messagehandlers;

import java.rmi.RemoteException;

import distributed.systems.core.Message;
import distributed.systems.network.ServerNode;

public class LogHandler implements MessageHandler {

	private static final String MESSAGE_TYPE = "LOG";
	private final ServerNode me;

	public LogHandler(ServerNode me) {
		this.me = me;
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		me.getServerSocket().logMessage(message);
		return null;
	}
}
