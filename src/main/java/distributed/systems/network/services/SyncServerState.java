package distributed.systems.network.services;

import java.rmi.RemoteException;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.network.NodeType;
import distributed.systems.network.ServerNode;
import distributed.systems.network.ServerState;

public class SyncServerState implements SocketService {
	public static final String MESSAGE_TYPE = "UPDATE_SERVERSTATE";
	public static final long UPDATE_INTERVAL = 10000;
	private final ServerNode me;

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}
	
	public SyncServerState(ServerNode me) {
		this.me = me;
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		me.updateOtherServerState((ServerState) message.get("serverstate"));
		return null;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(UPDATE_INTERVAL);
			while(!Thread.currentThread().isInterrupted()) {
				sendUpdatedState();
				Thread.sleep(UPDATE_INTERVAL);
			}
		} catch (InterruptedException e) {
			me.safeLogMessage("SyncServerState service has been stopped", LogType.INFO);
		}
	}

	public void sendUpdatedState() {
		me.safeLogMessage("Sending updated serverState to all connected servers!", LogType.DEBUG);
		Message message = me.getMessageFactory().createMessage(MESSAGE_TYPE)
				.put("serverstate", me.getServerState());
		me.getServerSocket().broadcast(message, NodeType.SERVER);
		me.getServerState().getClients().stream()
				.forEach(client -> me.getSocket().sendMessage(message, client));
	}
}
