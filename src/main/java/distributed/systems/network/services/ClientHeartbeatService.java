package distributed.systems.network.services;

import java.util.List;

import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.network.AbstractClientNode;
import distributed.systems.network.AbstractNode;
import distributed.systems.network.ConnectionException;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeType;

public class ClientHeartbeatService extends HeartbeatService {

	private final ExtendedSocket localsocket;
	private final AbstractClientNode clientNode;

	public ClientHeartbeatService(AbstractClientNode me, ExtendedSocket socket) {
		super(me, socket);
		this.localsocket = socket;
		this.clientNode = me;
	}

	// TODO: do some cleanup, moving the clients of a disconnected server to other servers
	protected void removeNode(NodeAddress address) {
		watchNodes.remove(address);
		if(clientNode.getServerAddress().equals(address)) {
			clientNode.joinBackupServer();
		}
		socket.logMessage("Node `" + address.getName() + "` TIMED OUT, because it has not been sending any heartbeats!",
				LogType.WARN);
	}

	public void doHeartbeat() {
		Message message = me.getMessageFactory().createMessage(HeartbeatService.MESSAGE_TYPE);
		try {
			localsocket.sendMessage(message, localsocket.getRegistryAddress());
		}
		catch (ConnectionException e) {
			localsocket.logMessage("Failed to send message to server `"
					+ localsocket.getRegistryAddress() + "`; message: " + message + ", because: " + e, LogType.ERROR);
		}
	}
}
