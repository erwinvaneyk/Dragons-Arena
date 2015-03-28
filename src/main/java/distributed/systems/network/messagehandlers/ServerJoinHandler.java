package distributed.systems.network.messagehandlers;

import java.util.ArrayList;

import com.sun.istack.internal.NotNull;
import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.core.Socket;
import distributed.systems.network.AbstractServerNode;
import distributed.systems.network.LocalSocket;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerAddress;
import distributed.systems.network.ServerNode;

public class ServerJoinHandler implements MessageHandler {

	public static final String MESSAGE_TYPE = "HANDSHAKE";

	private final ServerNode me;
	private final MessageFactory messageFactory;

	public ServerJoinHandler(ServerNode node) {
		this.me = node;
		this.messageFactory = me.getMessageFactory();
	}


	/**
	 * Sender:      New Server
	 * Receiver:    Existing server in cluster
	 * Result:      server is added
	 *
	 * Message to cluster:
	 * - MessageType: HANDSHAKE
	 * - address:   address of the new server
	 *
	 * Return message
	 * - MessageType: HANDSHAKE
	 * - servers: all servers including self
	 * - address as decided by the servers
	 *
	 */
	public static void connectToCluster(AbstractServerNode server, NodeAddress hostAddress) {
		if(server.getOtherNodes().stream().anyMatch(node -> node.equals(hostAddress))) {
			server.getServerSocket().logMessage("Node tried to connect to cluster, even though it already is connected!",
					LogType.WARN);
			return;
		}
		Socket socket = LocalSocket.connectTo(hostAddress);
		Message response = socket.sendMessage(
				server.getMessageFactory().createMessage(MESSAGE_TYPE).put("address", server.getServerAddress()),
				hostAddress);
		// TODO: error handling

		// Assume all is good
		ArrayList<ServerAddress> otherServers = (ArrayList<ServerAddress>) response.get("servers");
		ServerAddress verifiedAddress = (ServerAddress) response.get("address");
		server.getAddress().setId(verifiedAddress.getId());
		server.updateBindings();
		server.getOtherNodes().addAll(otherServers);
		server.getServerSocket().logMessage(
				"Server `" + server.getAddress() + "` successfully connected to the cluster, other nodes found: "
						+ server.getOtherNodes(), LogType.INFO);
	}

	public Message onMessageReceived(Message message) {
		Message response = null;
		ServerAddress newServer = (ServerAddress) message.get("address");
		// TODO: check if address is unique
		if(message.get("forwarded") == null) {
			ArrayList<ServerAddress> servers = new ArrayList<>(me.getOtherNodes());
			servers.add(me.getServerAddress());
			response = messageFactory.createMessage(message.getMessageType())
					.put("servers", servers);

			// Determine id for new server if necessary
			if(newServer.getId() < 0) { // Indicates that server has no id yet
				String oldServerId = newServer.toString();
				newServer.setId(me.generateUniqueId(newServer));
				response.put("address", newServer);
				message.put("address", newServer);
				me.getServerSocket().logMessage(
						"Assigned ID to new server, renamed `" + oldServerId + "` to `" + newServer + "`, otherNodes: " + me
								.getOtherNodes()
								+ ".", LogType.DEBUG);
			}
			// Propagate
			message.put("forwarded", true);
			me.getServerSocket().broadcast(message);
		}
		// Add new server to list
		me.getOtherNodes().add(new ServerAddress(newServer));
		me.getServerSocket()
				.logMessage("Added node to list of connected nodes, now the list is: " + me.getOtherNodes() + ".", LogType.DEBUG);
		return response;
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}
}
