package distributed.systems.network.messagehandlers;

import java.util.ArrayList;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.network.AbstractServerNode;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeState;
import distributed.systems.network.ServerNode;
import distributed.systems.network.ServerState;

public class ServerConnectHandler implements MessageHandler {

	public static final String MESSAGE_TYPE = "HANDSHAKE";

	private final AbstractServerNode me;
	private final MessageFactory messageFactory;

	public ServerConnectHandler(AbstractServerNode node) {
		this.me = node;
		this.messageFactory = me.getMessageFactory();
	}

	public Message onMessageReceived(Message message) {
		Message response = null;
		NodeAddress newAddress = (NodeAddress) message.get("address");
		ServerState newServer = (ServerState) message.get("newServer");
		// TODO: check if address is unique

		if(message.get("forwarded") == null) {
			ServerNode meServer = (ServerNode) me;

			ArrayList<NodeState> servers = new ArrayList<>(me.getConnectedNodes().keySet()); //TODO add loggers
			servers.add(me.getNodeState());
			response = messageFactory.createMessage(message.getMessageType())
					.put("servers", servers);

			// Determine id for new server if necessary
			if(newAddress.getId() < 0) { // Indicates that server has no id yet
				String oldServerId = newAddress.toString();
				newAddress.setId(me.generateUniqueId(newAddress.getType()));
				newServer = new ServerState(meServer.getServerState().getBattleField(), newAddress, newAddress.getType());
				message.put("newServer", newServer);
				response.put("newServer", newServer);
				me.getServerSocket().logMessage(
						"Assigned ID to new server, renamed `" + oldServerId + "` to `" + newAddress + "`, other servers: " + me.getConnectedNodes().keySet() + ".", LogType.DEBUG);
			}
			// Propagate
			message.put("forwarded", true);
			me.getServerSocket().broadcast(message);
		}
		// Add new server to list
		me.addServer(newServer);
		return response;
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}
}
