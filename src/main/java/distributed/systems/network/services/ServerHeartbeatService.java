package distributed.systems.network.services;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.network.AbstractNode;
import distributed.systems.network.NodeType;
import distributed.systems.network.ServerAddress;
import distributed.systems.network.ServerSocket;

public class ServerHeartbeatService extends HeartbeatService {

	private final ServerSocket serversocket;

	public ServerHeartbeatService(AbstractNode me, ServerSocket socket) {
		super(me, socket);
		this.serversocket = socket;
	}

	// TODO: do some cleanup, moving the clients of a disconnected server to other servers
	protected void removeNode(ServerAddress address) {
		nodes.remove(address);
		watchNodes.remove(address);
		socket.logMessage("Node `" + address.getName() + "` TIMED OUT, because it has not been sending any heartbeats!",
				LogType.WARN);

	}

	public void doHeartbeat() {
		Message message = me.getMessageFactory().createMessage(HeartbeatService.MESSAGE_TYPE);
		// Send to other servers
		socket.broadcast(message, NodeType.SERVER);
		// Send to my clients
		serversocket.getMe().getServerAddress().getClients().stream().forEach(node -> {
			try {
				socket.sendMessage(message, node);
			}
			catch (RuntimeException e) {
				socket.logMessage(
						"Failed to send message to node `" + node + "`; message: " + message + ", because: "
								+ e, LogType.ERROR);
			}
		});

	}
}
