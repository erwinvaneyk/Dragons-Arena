package distributed.systems.network.services;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.network.AbstractNode;
import distributed.systems.network.AbstractServerNode;
import distributed.systems.network.ConnectionException;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeType;
import distributed.systems.network.ServerNode;
import distributed.systems.network.ServerSocket;
import distributed.systems.network.messagehandlers.ClientHandler;

public class ServerHeartbeatService extends HeartbeatService {

	private final ServerSocket serversocket;
	private final AbstractServerNode abstractServerNode;

	public ServerHeartbeatService(AbstractServerNode me, ServerSocket socket) {
		super(me, socket);
		this.serversocket = socket;
		this.abstractServerNode = me;
	}

	// TODO: do some cleanup, moving the clients of a disconnected server to other servers
	protected void removeNode(NodeAddress address) {
		watchNodes.remove(address);
		if(me.getAddress().isServer() && address.isClient()) {
			ServerNode meServer = ((ServerNode) me);
			serversocket.logMessage("Removing client from the battlefield!", LogType.DEBUG);
			meServer.removeClient(address);
			Message serverMessage = me.getMessageFactory()
					.createMessage(ClientHandler.MESSAGE_TYPE)
					.put("subject", "remove")
					.put("client", address);
			serversocket.broadcast(serverMessage, NodeType.SERVER);
		}
		abstractServerNode.getNodeState().getConnectedNodes().remove(address);
		abstractServerNode.getConnectedNodes().stream()
				.filter(node -> node.getAddress().equals(address))
				.findAny()
				.ifPresent(node -> abstractServerNode.getConnectedNodes().remove(node));
		serversocket.logMessage("Node `" + address.getName() + "` TIMED OUT, because it has not been sending any heartbeats!",
				LogType.WARN);

	}

	public void doHeartbeat() {
		Message message = me.getMessageFactory().createMessage(HeartbeatService.MESSAGE_TYPE);
		Message messageForClients = me.getMessageFactory().createMessage(HeartbeatService.MESSAGE_TYPE);
		messageForClients.put("otherServers", new ArrayList<>(abstractServerNode.getConnectedNodes()
				.stream()
				.filter(node -> node.getAddress().isServer())
				.collect(toList())));

		// Send to my watchNodes
		watchNodes.entrySet().stream().forEach(node -> {
			try {
				if(node.getKey().isClient()) {
					serversocket.sendMessage(messageForClients, node.getKey());
				} else {
					serversocket.sendMessage(message, node.getKey());
				}
			} catch (RuntimeException | ConnectionException e) {
				serversocket.logMessage(
						"Failed to send message to node `" + node + "`; message: " + message + ", because: "
								+ e, LogType.ERROR);
			}
		});
	}
}