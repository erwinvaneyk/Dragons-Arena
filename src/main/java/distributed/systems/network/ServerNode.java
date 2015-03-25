package distributed.systems.network;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.das.BattleField;
import distributed.systems.das.presentation.BattleFieldViewer;
import distributed.systems.network.services.HeartbeatService;
import distributed.systems.network.services.LoadBalanceService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A single server node
 *
 * Will also need to take care of the dragons (with fault tolerance)
 */
@ToString
public class ServerNode extends UnicastRemoteObject implements IMessageReceivedHandler {
	private final ExecutorService services = Executors.newCachedThreadPool();

	@Getter
	private ServerSocket serverSocket;

	@Getter @Setter
	private BattleField battlefield;

	@Getter
	private NodeAddress address;

	private RegistryNode ownRegistry;
	private HeartbeatService heartbeatService;
	private LoadBalanceService loadBalanceService;
	private LocalSocket socket;

	public static void main(String[] args) throws RemoteException {
		new ServerNode(RegistryNode.PORT);
	}

	public ServerNode(int port, boolean newCluster) throws RemoteException {
		setup(port, newCluster);
	}

	public ServerNode(int port) throws RemoteException {
		setup(port, false);
	}

	public void setup(int port, boolean newCluster) throws RemoteException {
		// Set own ownRegistry
		address = new NodeAddress(port, NodeAddress.NodeType.SERVER);
		ownRegistry = new RegistryNode(port);
		// Setup server socket
		serverSocket = new ServerSocket(this);
		socket = LocalSocket.connectTo(address);
		socket.register(address.toString());

		if (newCluster) {
			serverSocket.logMessage("Starting new cluster, starting with id 0", LogType.DEBUG);
			address.setId(0);
			// Add handlers to registry
			socket.register(address.toString());
			socket.addMessageReceivedHandler(this);
			// Setup battlefield
			battlefield = BattleField.getBattleField();
			battlefield.setServerSocket(socket);
			battlefield.setMessagefactory(serverSocket.getMessageFactory());
			serverSocket.logMessage("Created the battlefield.", LogType.DEBUG);
		}

		// setup services
		heartbeatService = new HeartbeatService(serverSocket.getOtherNodes(), serverSocket, serverSocket.getMessageFactory());
		loadBalanceService = new LoadBalanceService(serverSocket, address);
		services.submit(heartbeatService);


		// TODO: Setup battlefield (self or from network)
		// TODO: start a dragon (if necessary)

		serverSocket.logMessage("Server (" + address + ") is up and running", LogType.INFO);
		new Thread(BattleFieldViewer::new).start();

	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		message.setReceivedTimestamp();
		switch (message.getMessageType()) {
			case HANDSHAKE:
				return serverSocket.onConnectToCluster(message);
			case HEARTBEAT:
				return heartbeatService.onMessageReceived(message);
			case LOG:
				// Propagate log-messages (from clients or anyone else) to log nodes
				serverSocket.logMessage(message);
				break;
			case JOIN_SERVER:
				return loadBalanceService.onMessageReceived(message);
			case SYNC_BATTLEFIELD:
				return onSyncBattlefield(message);
			case GENERIC:
				// TODO: task distribution
				serverSocket.logMessage(message);
				return battlefield.onMessageReceived(message);
			default:
				serverSocket.logMessage("Unknown type of message: " + message + "! Ignoring the message", LogType.WARN);
				break;
		}
		return null;
	}

	public Message onSyncBattlefield(Message message) {
		// TODO: desyncronized battlefields fix
		Message response = serverSocket.getMessageFactory().createMessage(Message.Type.SYNC_BATTLEFIELD)
				.put("battlefield", battlefield);
		return response;
	}


	public void disconnect() {
		// Unregister the API
		socket.unRegister();

		// Stop services
		services.shutdownNow();

		try {
			// Stop exporting this object
			UnicastRemoteObject.unexportObject(this, true);
			serverSocket.logMessage("Disconnected `" + address + "`.", LogType.INFO);
		}
		catch (NoSuchObjectException e) {
			e.printStackTrace();
		}
	}
}