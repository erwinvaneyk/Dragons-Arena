package distributed.systems.network.messagehandlers;

import java.rmi.RemoteException;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.network.ClientNode;

public class ClientGameActionHandler implements MessageHandler {


	private static final String MESSAGE_TYPE = "DEFAULT";
	private final ClientNode me;

	public ClientGameActionHandler(ClientNode me) {
		this.me = me;
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		me.getSocket().logMessage("[" + me.getAddress() + "] received message: (" + message + ")", LogType.DEBUG);
		return me.getUnit().onMessageReceived(message);
	}
}
