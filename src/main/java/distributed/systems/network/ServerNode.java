package distributed.systems.network;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import com.sun.istack.internal.NotNull;
import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.LogType;
import distributed.systems.core.MessageFactory;
import distributed.systems.das.BattleField;
import distributed.systems.das.presentation.BattleFieldViewer;
import distributed.systems.network.messagehandlers.ServerGameActionHandler;
import distributed.systems.network.messagehandlers.LogHandler;
import distributed.systems.network.messagehandlers.ServerJoinHandler;
import distributed.systems.network.messagehandlers.SyncBattlefieldHandler;
import distributed.systems.network.services.HeartbeatService;
import distributed.systems.network.services.NodeBalanceService;
import distributed.systems.network.services.ServerHeartbeatService;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang.SerializationUtils;


/**
 * A single server node
 *
 * Will also need to take care of the dragons (with fault tolerance)
 */
@ToString
public class ServerNode extends BasicNode implements IMessageReceivedHandler {

	@Getter
	private final ArrayList<ServerAddress> otherNodes = new ArrayList<>();

	@Getter
	private BattleField battlefield;

	// Own registry
	private RegistryNode ownRegistry;
	private NodeAddress currentBinding;

	@Getter
	private ServerSocket serverSocket;

	private HeartbeatService heartbeatService;
	private NodeBalanceService nodeBalanceService;


	public static void main(String[] args) throws RemoteException {
		new ServerNode(RegistryNode.PORT, false);
	}

	public ServerNode(int port, boolean newCluster) throws RemoteException {
		// Parent requirements
		ownRegistry = new RegistryNode(port);
		address = new ServerAddress(port, NodeAddress.NodeType.SERVER);
		socket = LocalSocket.connectTo(address);
		socket.register(address);
		messageFactory = new MessageFactory(address);
		// Setup message-handlers
		addMessageHandler(new ServerJoinHandler(this));
		addMessageHandler(new SyncBattlefieldHandler(this));
		addMessageHandler(new LogHandler(this));
		// Setup server localSocket
		serverSocket = new ServerSocket(this, otherNodes);

		if (newCluster) {
			serverSocket.logMessage("Starting new cluster, starting with id 0", LogType.DEBUG);
			address.setId(0);
			updateBindings();
			// Setup battlefield
			battlefield = new BattleField();
			battlefield.setServerSocket(socket);
			battlefield.setMessagefactory(messageFactory);
			serverSocket.logMessage("Created the battlefield.", LogType.DEBUG);
		}

		// setup services
		heartbeatService = new ServerHeartbeatService(this, serverSocket, otherNodes);
		nodeBalanceService = new NodeBalanceService(this);
		addMessageHandler(heartbeatService);
		addMessageHandler(nodeBalanceService);
		addMessageHandler(new ServerGameActionHandler(this));
		System.out.println(ownRegistry);
		runService(heartbeatService);

		// TODO: start a dragon (if necessary)
		serverSocket.logMessage("Server (" + address + ") is up and running", LogType.INFO);
	}

	public ServerAddress getServerAddress() {
		return (ServerAddress) address;
	}

	public int generateUniqueId(@NotNull NodeAddress oldAddress) {
		ArrayList<NodeAddress> nodes = new ArrayList<>(getOtherNodes());
		nodes.add(getAddress());
		int highestId = nodes
				.stream()
				.filter(node -> node.getType().equals(oldAddress.getType()))
				.mapToInt(NodeAddress::getId)
				.max().orElse(Math.max(getAddress().getId(), 0)) + 1;
		return highestId;
	}

	public void connect(NodeAddress server) {
		ServerJoinHandler.connectToCluster(this, server);
		battlefield = SyncBattlefieldHandler.syncBattlefield(this);
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

	public void launchViewer() {
		if(battlefield != null) {
			new BattleFieldViewer(battlefield);
		} else {
			safeLogMessage("Cannot launch battlefield-viewer; no battlefield available!", LogType.ERROR);
		}

	}
}