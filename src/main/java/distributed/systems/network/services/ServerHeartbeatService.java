package distributed.systems.network.services;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.network.AbstractNode;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeType;
import distributed.systems.network.ServerNode;
import distributed.systems.network.ServerSocket;
import distributed.systems.network.messagehandlers.ClientHandler;

public class ServerHeartbeatService extends HeartbeatService {

	private final ServerSocket serversocket;

	public ServerHeartbeatService(AbstractNode me, ServerSocket socket) {
		super(me, socket);
		this.serversocket = socket;
	}

	// TODO: do some cleanup, moving the clients of a disconnected server to other servers
	protected void removeNode(NodeAddress address) {
		watchNodes.remove(address);
		if(me.getNodeType().equals(NodeType.SERVER) && (address.getType().equals(NodeType.DRAGON) || address.getType().equals(NodeType.PLAYER))) {
			serversocket.logMessage("Removing client from the battlefield!", LogType.DEBUG);
			((ServerNode) me).removeClient(address);
			Message serverMessage = me.getMessageFactory().createMessage(ClientHandler.MESSAGE_TYPE).put("subject", "remove").put("client", address);
			serversocket.broadcast(serverMessage, NodeType.SERVER);
		}
		serversocket.logMessage("Node `" + address.getName() + "` TIMED OUT, because it has not been sending any heartbeats!",
				LogType.WARN);

	}

	public void doHeartbeat() {
		Message message = me.getMessageFactory().createMessage(HeartbeatService.MESSAGE_TYPE);
		// Send to my watchNodes
		watchNodes.entrySet().stream().forEach(node -> {
			try {
				serversocket.sendMessage(message, node.getKey());
			} catch (RuntimeException e) {
				serversocket.logMessage("Failed to send message to node `" + node + "`; message: " + message + ", because: "
								+ e, LogType.ERROR);
			}
		});

	}
}
