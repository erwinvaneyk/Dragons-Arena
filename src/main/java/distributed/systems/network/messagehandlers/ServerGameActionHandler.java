package distributed.systems.network.messagehandlers;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.das.MessageRequest;
import distributed.systems.network.NodeType;
import distributed.systems.network.ServerNode;

import java.rmi.RemoteException;

public class ServerGameActionHandler implements MessageHandler {


	private static final String MESSAGE_TYPE = "DEFAULT";
	private final ServerNode me;

	public ServerGameActionHandler(ServerNode me) {
		this.me = me;
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		me.getSocket().logMessage("[" + me.getAddress() + "] received message: ("  + message + ")", LogType.DEBUG);

		boolean isAllowed = true;
		MessageRequest request = (MessageRequest)message.get("request");
		if(MessageRequest.moveUnit.equals(request)) {
			SynchronizedGameActionHandler synchronizedHandler = me.getSynchronizedGameActionHandler();
			isAllowed = synchronizedHandler.synchronizeAction(message);
		}

		if(isAllowed) {
			Message response = me.getServerState().getBattleField().onMessageReceived(message);
			// Notify other servers
			if (response != null && message.get("update").equals(true)) {
				message.put("update", false);
				me.getServerSocket().broadcast(response, NodeType.SERVER);
			}
		}
        return null;
	}
}
