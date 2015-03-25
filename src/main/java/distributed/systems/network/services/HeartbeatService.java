package distributed.systems.network.services;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.core.Socket;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerAddress;
import distributed.systems.network.ServerSocket;

public class HeartbeatService implements Runnable, SocketService {

	private static final int TIMEOUT_DURATION = 15000;
	private static final int CHECK_INTERVAL = 5000;

	private final List<ServerAddress> heartbeatNodes;
	private final Socket socket;
	private final MessageFactory messageFactory;
	private Map<NodeAddress, Integer> nodes = new ConcurrentHashMap<>();

	public HeartbeatService(List<ServerAddress> heartbeatNodes, Socket socket, MessageFactory messageFactory) {
		this.heartbeatNodes = heartbeatNodes;
		this.socket = socket;
		this.messageFactory = messageFactory;
		updateNodes();
	}


	public void run() {
		socket.logMessage("Starting heartbeat process...", LogType.DEBUG);
		try {
			while(!Thread.currentThread().isInterrupted()) {
				doHeartbeat();
				checkHeartbeats();
				Thread.sleep(CHECK_INTERVAL);
			}
		} catch (InterruptedException e) {
			socket.logMessage("Heartbeat service has been stopped", LogType.INFO);
		}
	}

	private void updateNodes() {
		heartbeatNodes.stream()
				.filter(node -> !nodes.containsKey(node))
				.forEach(node -> nodes.put(node, TIMEOUT_DURATION / CHECK_INTERVAL));
	}

	private void checkHeartbeats() {
		// Check for new nodes
		updateNodes();
		// Count down nodes
		nodes.entrySet().stream().forEach(node -> {
			if(node.getValue() < 0) {
				removeNode(node.getKey());
			} else {
				nodes.put(node.getKey(), node.getValue() - 1);
			}
		});
	}

	// TODO: do some cleanup, moving the clients of a disconnected server to other servers
	private void removeNode(NodeAddress address) {
		nodes.remove(address);
		heartbeatNodes.remove(address);
		socket.logMessage("Node `" + address.getName() + "` TIMED OUT, because it has not been sending any heartbeats!",
				LogType.WARN);

	}

	public void doHeartbeat() {
		// TODO: also broadcast to clientnodes
		Message message = messageFactory.createMessage(Message.Type.HEARTBEAT);
		socket.broadcast(message, NodeAddress.NodeType.SERVER);
		// Fast hack to get my clients
		if(socket instanceof ServerSocket) {
			ServerSocket serversocket = (ServerSocket) socket;
			serversocket.getMe().getAddress().getClients().stream().forEach(node -> {
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

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		NodeAddress origin = message.getOrigin();
		nodes.entrySet().stream()
				.filter(node -> node.getKey().equals(origin))
				.findAny()
				.ifPresent(node -> {
					node.setValue(TIMEOUT_DURATION / CHECK_INTERVAL);
					socket.logMessage("Received a heartbeat from node `" + node.getKey().getName() + "` (" + nodes +").",
							LogType.DEBUG);
				});
		return null;
	}
}
