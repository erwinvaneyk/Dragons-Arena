package distributed.systems.network;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import com.sun.istack.internal.NotNull;
import distributed.systems.core.LogType;
import distributed.systems.core.MessageFactory;
import distributed.systems.network.messagehandlers.ServerJoinHandler;
import lombok.Getter;
import org.apache.commons.lang.SerializationUtils;

public abstract class AbstractServerNode extends AbstractNode {

	@Getter
	protected final ArrayList<ServerAddress> otherNodes = new ArrayList<>();

	@Getter
	protected ServerSocket serverSocket;

	public AbstractServerNode(int port) throws RemoteException {
		// Parent requirements
		ownRegistry = new RegistryNode(port);
		address = new ServerAddress(port, this.getNodeType());
		socket = LocalSocket.connectTo(address);
		socket.register(address);
		messageFactory = new MessageFactory(address);
		serverSocket = new ServerSocket(this);
	}

	// Own registry
	protected RegistryNode ownRegistry;
	private NodeAddress currentBinding;

	protected AbstractServerNode() throws RemoteException {}

	public void connect(NodeAddress server) {
		ServerJoinHandler.connectToCluster(this, server);
	}

	public int generateUniqueId(@NotNull NodeAddress oldAddress) {
		ArrayList<NodeAddress> nodes = new ArrayList<>(otherNodes);
		nodes.add(getAddress());
		int highestId = nodes
				.stream()
				.filter(node -> node.getType().equals(oldAddress.getType()))
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
}
