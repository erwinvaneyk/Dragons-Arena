package distributed.systems.network.messagehandlers;

import java.rmi.RemoteException;

import distributed.systems.core.Message;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerNode;

public class ClientHandler implements MessageHandler {

	public static final String MESSAGE_TYPE = "CLIENT";
	public static final String REMOVE = "remove";
	private final ServerNode me;

	public ClientHandler(ServerNode server) {
		this.me = server;
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		String subject = (String) message.get("subject");
		if(subject != null) {
			switch (subject) {
				case REMOVE:
					me.removeClient((NodeAddress) message.get("client"));
					break;
			}
		}
		return null;
	}
}
