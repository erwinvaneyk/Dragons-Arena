package distributed.systems.network;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

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
	protected final ArrayList<ServerAddress> otherNodes = new ArrayList<>();

	@Getter
	protected final ServerSocket serverSocket;

	// Own registry
	protected final RegistryNode ownRegistry;
	private NodeAddress currentBinding;

	public AbstractServerNode(int port) throws RemoteException {
		// Parent requirements
		ownRegistry = new RegistryNode(port);
		address = new ServerAddress(port, this.getNodeType());
		socket = LocalSocket.connectTo(address);
		socket.register(address);
		messageFactory = new MessageFactory(address);
		serverSocket = new ServerSocket(this);
	}

	public void connect(NodeAddress server) {
		if(otherNodes.stream().anyMatch(node -> node.equals(server))) {
			serverSocket.logMessage("Node tried to connect to cluster, even though it already is connected!",
					LogType.WARN);
			return;
		}
		Socket socket = LocalSocket.connectTo(server);
		Message response = socket.sendMessage(
				messageFactory.createMessage(ServerConnectHandler.MESSAGE_TYPE).put("address", getServerAddress()), server);
		// TODO: error handling

		// Assume all is good
		ArrayList<ServerAddress> otherServers = (ArrayList<ServerAddress>) response.get("servers");
		ServerAddress verifiedAddress = (ServerAddress) response.get("address");
		address.setId(verifiedAddress.getId());
		updateBindings();
		otherNodes.addAll(otherServers);
		serverSocket.logMessage("Server `" + address + "` successfully connected to the cluster, other nodes found: "
						+ otherNodes, LogType.INFO);
	}

	public int generateUniqueId(@NotNull NodeType type) {
		ArrayList<NodeAddress> nodes = new ArrayList<>(otherNodes);
		nodes.add(getAddress());
		int highestId = nodes
				.stream()
				.filter(node -> node.getType().equals(type))
				.mapToInt(NodeAddress::getId)
				.max().orElse(Math.max(getAddress().getId(), 0)) + 1;
		return highestId;
	}

	// Hacky
	public ServerAddress getServerAddress() {
		return (ServerAddress) address;
	}

	public void updateBindings() {
		// Check if there is a difference
		if (address.equals(currentBinding)) {
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
		socket = LocalSocket.connectTo(address);
		socket.register(address);

		socket.addMessageReceivedHandler(this);
		safeLogMessage("Successfully rebounded the binding " + currentBinding + " to " + address, LogType.DEBUG);
		currentBinding = (NodeAddress) SerializationUtils.clone(address);
	}

	public void disconnect() throws RemoteException {
		super.disconnect();
		UnicastRemoteObject.unexportObject(this, true);
		ownRegistry.disconnect();
	}

	public void addServer(ServerAddress server) {
		otherNodes.add(server);
	}

	public void startCluster() {
		// TODO: say goodbye to othernodes
		otherNodes.clear();
		serverSocket.logMessage("Starting new cluster, starting with id 0", LogType.DEBUG);
		address.setId(0);
		updateBindings();
	}
}
