package distributed.systems.network.messagehandlers;

import java.rmi.RemoteException;

import distributed.systems.core.Message;
import distributed.systems.network.AbstractClientNode;
import distributed.systems.network.ServerState;
import distributed.systems.network.services.SyncServerState;

public class ClientServerSyncHandler implements MessageHandler {

	private final AbstractClientNode me;

	@Override
	public String getMessageType() {
		return SyncServerState.MESSAGE_TYPE;
	}

	public ClientServerSyncHandler(AbstractClientNode me) {
		this.me = me;
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		ServerState serverState = (ServerState) message.get("serverstate");
		me.setBackupServers(serverState.getConnectedNodes());
		return null;
	}
}
