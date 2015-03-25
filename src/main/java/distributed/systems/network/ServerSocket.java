package distributed.systems.network;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.core.Socket;
import distributed.systems.das.BattleField;
import lombok.Getter;

/**
 * Deals with connections and messages between servers
 */
public class ServerSocket implements Socket {


	/**
	 * Connections to other servers in the cluster
	 */
	@Getter
	private final ArrayList<NodeAddress> otherNodes = new ArrayList<>();
	@Getter
	private final ArrayList<NodeAddress> myClients = new ArrayList<>();
	private final ServerNode me;
	@Getter
	private final MessageFactory messageFactory;

	private boolean isConnected = false;


	public ServerSocket(ServerNode me) {
		this.me = me;
		this.messageFactory = new MessageFactory(me.getAddress());
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
	public void connectToCluster(NodeAddress hostAddress) {
		if(otherNodes.stream().anyMatch(node -> node.equals(hostAddress))) {
			logMessage("Node tried to connect to cluster, even though it already is connected!", LogType.WARN);
			return;
		}
		Socket socket = LocalSocket.connectTo(hostAddress);
		Message response = socket.sendMessage(
				messageFactory.createMessage(Message.Type.HANDSHAKE).put("address", me.getAddress()),
				hostAddress);
		// TODO: error handling

		// Assume all is good
		ArrayList<NodeAddress> otherServers = (ArrayList<NodeAddress>) response.get("servers");
		NodeAddress verifiedAddress = (NodeAddress) response.get("address");
		me.getAddress().setId(verifiedAddress.getId());
		// Add handlers to registry
		ExtendedSocket mySocket = LocalSocket.connectTo(me.getAddress());
		mySocket.register(me.getAddress().toString());
		mySocket.addMessageReceivedHandler(me);
		otherNodes.addAll(otherServers);
		logMessage("Server `" + me.getAddress() + "` successfully connected to the cluster, other nodes found: " + otherNodes, LogType.INFO);
		me.setBattlefield(syncBattlefield());
	}

	public Message onConnectToCluster(Message message) {
		Message response = null;
		NodeAddress newServer = (NodeAddress) message.get("address");
		// TODO: check if address is unique
		if(message.get("forwarded") == null) {
			ArrayList<NodeAddress> servers = new ArrayList<>(otherNodes);
			servers.add(me.getAddress());
			response = messageFactory.createMessage(message.getMessageType())
					.put("servers", servers);

			// Determine id for new server if necessary
			if(newServer.getId() < 0) { // Indicates that server has no id yet
				newServer = generateUniqueId(newServer);
				response.put("address", newServer);
				message.put("address", newServer);
				logMessage("Assigned ID to new server, named it: `" + newServer + "`, otherNodes: " + otherNodes + ".", LogType.DEBUG);
			}
			// Propagate
			message.put("forwarded", true);
			broadcast(message);
		}
		// Add new server to list
		otherNodes.add(newServer);
		logMessage("Added node to list of connected nodes, now the list is: " + otherNodes + ".", LogType.DEBUG);
		return response;
	}


	public BattleField syncBattlefield() {
		Message message = messageFactory.createMessage(Message.Type.SYNC_BATTLEFIELD);

		List<Message> battlefields = otherNodes.stream()
				.filter(NodeAddress::isServer)
				.flatMap(server -> {
					logMessage("Requesting battlefield from `" + server + "`.", LogType.DEBUG);
					try {
						return Stream.of(sendMessage(message, server));
					} catch (RuntimeException e) {
						e.printStackTrace();
						logMessage(
								"Failed to send message to node `" + server + "`; message: " + message + ", because: "
										+ e, LogType.ERROR);
						return Stream.empty();
					}
				}).collect(toList());
		logMessage("Syncing battlefield, received " + battlefields.size() + " battlefields.", LogType.DEBUG);


		// Election (byzantine)
		BattleField battleField = battlefields.stream()
				.map(node -> (BattleField) node.get("battlefield"))
				.collect(Collectors.groupingBy(w -> w, Collectors.counting()))
				.entrySet()
				.stream()
				.reduce((a, b) -> a.getValue() > b.getValue() ? a : b)
				.map(Map.Entry::getKey)
				.get();
		battleField.setMessagefactory(messageFactory);
		battleField.setServerSocket(LocalSocket.connectTo(me.getAddress()));
		logMessage("Synced battlefield, choose battlefield with hash `" + battleField.hashCode() + "`", LogType.DEBUG);
		return battleField;
	}


	public NodeAddress generateUniqueId(NodeAddress oldAddress) {
		ArrayList<NodeAddress> nodes = new ArrayList<>(otherNodes);
		nodes.add(me.getAddress());
		int highestId = nodes
				.stream()
				.filter(node -> node.getType().equals(oldAddress.getType()))
				.mapToInt(NodeAddress::getId)
				.max().orElse(Math.max(me.getAddress().getId(), 0)) + 1;
		return new NodeAddress(oldAddress.getType(), highestId, oldAddress.getPhysicalAddress());
	}


	@Override @Deprecated
	public Message sendMessage(Message message, String destination) {
		return sendMessage(message, NodeAddress.fromAddress(destination));
	}

	/**
	 * Currently only for servers
	 */
	public Message sendMessage(Message message, NodeAddress destination) {
		Socket socket = LocalSocket.connectTo(destination);
		socket.logMessage(destination.toString(), LogType.DEBUG);
		return socket.sendMessage(message, destination.toString());
	}

	public void broadcast(Message message) {
		otherNodes.stream().forEach(node -> {
			try {
				sendMessage(message, node);
			}
			catch (RuntimeException e) {
				logMessage("Failed to send message to node `" + node + "`; message: " + message + ", because: " + e,
						LogType.ERROR);
			}
		});
	}

	public void broadcast(Message message, NodeAddress.NodeType type) {
		otherNodes.stream()
				.filter(node -> node.getType().equals(type))
				.forEach(node -> {
					try {
						sendMessage(message, node);
					}
					catch (RuntimeException e) {
						logMessage("Failed to send message to node `" + node + "`; message: " + message + ", because: "
								+ e, LogType.ERROR);
					}
				});
	}

	public void logMessage(Message logMessage) {
		List<NodeAddress> logNodes = otherNodes
				.stream()
				.filter(node -> node.getType().equals(NodeAddress.NodeType.LOGGER))
				.collect(toList());
		logNodes.forEach(logger -> sendMessage(logMessage, logger));
		// If there are no loggers, just output it to the screen.
		if(logNodes.isEmpty()) {
			System.out.println("No logger present: " + logMessage);
		}
	}

	public void logMessage(String message, LogType type) {
		logMessage(messageFactory.createLogMessage(message, type));
	}
}
