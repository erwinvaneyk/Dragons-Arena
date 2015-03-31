package distributed.systems.network;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.istack.internal.NotNull;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.core.Socket;
import distributed.systems.network.messagehandlers.ServerConnectHandler;
import lombok.Getter;
import org.apache.commons.lang.SerializationUtils;

public abstract class AbstractServerNode extends AbstractNode {

	@Getter
	private Map<NodeState, Long> connectedNodes = new HashMap<>();

	@Getter
	protected NodeState nodeState;

	@Getter
	protected final ServerSocket serverSocket;

	// Own registry
	protected final RegistryNode ownRegistry;
	private NodeAddress currentBinding;

	public AbstractServerNode(int port) throws RemoteException {
		// Parent requirements
		ownRegistry = new RegistryNode(port);
		NodeAddress address = new NodeAddress(port, this.getNodeType());
		nodeState = new NodeState(address, getNodeType());
		socket = LocalSocket.connectTo(address);
		socket.register(address);
		messageFactory = new MessageFactory(nodeState);
		serverSocket = new ServerSocket(this);
	}

	public void connect(NodeAddress server) {
		if(connectedNodes.keySet().stream().anyMatch(s -> s.getAddress().equals(server))) {
			serverSocket.logMessage("Node tried to connect to cluster, even though it already is connected!",
					LogType.WARN);
			return;
		}
		Socket socket = LocalSocket.connectTo(server);
		Message response = socket.sendMessage(
				messageFactory.createMessage(ServerConnectHandler.MESSAGE_TYPE).put("address", getAddress()), server);
		// TODO: error handling

		// Assume all is good
		ArrayList<NodeState> otherServers = (ArrayList<NodeState>) response.get("servers");
		NodeState providedState = (NodeState) response.get("newServer");
		nodeState = providedState;
		messageFactory = new MessageFactory(nodeState);
		updateBindings();
		otherServers.stream().forEach(this::addServer);
		serverSocket.logMessage("Server `" + getAddress() + "` successfully connected to the cluster, other nodes found: "
				+ connectedNodes.keySet(), LogType.INFO);
	}

	public int generateUniqueId(@NotNull NodeType type) {
		ArrayList<NodeAddress> nodes = new ArrayList<>();//otherNodes);
		nodes.add(getAddress());
		int highestId = nodes
				.stream()
				.filter(node -> node.getType().equals(type))
				.mapToInt(NodeAddress::getId)
				.max().orElse(Math.max(getAddress().getId(), 0)) + 1;
		return highestId;
	}

	public void updateBindings() {
		// Check if there is a difference
		if (getAddress().equals(currentBinding)) {
			return;
		}

		// Remove old binding
		try {
			if(currentBinding != null) {
				ownRegistry.getRegistry().unbind(currentBinding.getName());
			}
		}
		catch (RemoteException | NotBoundException e) {
			safeLogMessage("Trying to unbind a non-existent binding: " + currentBinding + " from " + ownRegistry, LogType.WARN);
		}

		// Add new binding
		socket = LocalSocket.connectTo(getAddress());
		socket.register(getAddress());

		socket.addMessageReceivedHandler(this);
		safeLogMessage("Successfully rebounded the binding " + currentBinding + " to " + getAddress(), LogType.DEBUG);
		currentBinding = (NodeAddress) SerializationUtils.clone(getAddress());
	}

	public void disconnect() throws RemoteException {
		super.disconnect();
		UnicastRemoteObject.unexportObject(this, true);
		ownRegistry.disconnect();
	}

	public void addServer(NodeState node) {
		nodeState.getConnectedNodes().add(node.getAddress());
		connectedNodes.put(node, System.currentTimeMillis());
	}

	public void startCluster() {
		// TODO: say goodbye to othernodes
		//otherNodes.clear();
		serverSocket.logMessage("Starting new cluster, starting with id 0", LogType.DEBUG);
		getAddress().setId(0);
		updateBindings();
	}

	public NodeAddress getAddress() {
		return nodeState.getAddress();
	}
}
