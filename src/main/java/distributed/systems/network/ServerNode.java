package distributed.systems.network;

import static java.util.stream.Collectors.toList;

import javax.xml.soap.Node;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.LogType;
import distributed.systems.das.BattleField;
import distributed.systems.das.presentation.BattleFieldViewer;
import distributed.systems.network.messagehandlers.ClientHandler;
import distributed.systems.network.messagehandlers.ServerGameActionHandler;
import distributed.systems.network.messagehandlers.LogHandler;
import distributed.systems.network.messagehandlers.ServerConnectHandler;
import distributed.systems.network.messagehandlers.SyncBattlefieldHandler;
import distributed.systems.network.messagehandlers.SynchronizedGameActionHandler;
import distributed.systems.network.services.HeartbeatService;
import distributed.systems.network.services.NodeBalanceService;
import distributed.systems.network.services.ServerHeartbeatService;
import distributed.systems.network.services.SyncServerState;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;


/**
 * A single server node
 *
 * Will also need to take care of the dragons (with fault tolerance)
 */
@ToString
public class ServerNode extends AbstractServerNode implements IMessageReceivedHandler {

    private final HeartbeatService heartbeatService;
	private final NodeBalanceService nodeBalanceService;

	@Getter
	private final SynchronizedGameActionHandler synchronizedGameActionHandler;
	private final SyncServerState syncServerState;


	public static void main(String[] args) throws RemoteException {
		new ServerNode(RegistryNode.PORT);
	}

	public ServerNode(int port) throws RemoteException {
		super(port);
		// Setup message-handlers
		addMessageHandler(new LogHandler(this));
		addMessageHandler(new SyncBattlefieldHandler(this));
		addMessageHandler(new ServerConnectHandler(this));
		addMessageHandler(new ServerGameActionHandler(this));
		addMessageHandler(new ClientHandler(this));
		syncServerState = new SyncServerState(this);

		// setup services
		heartbeatService = new ServerHeartbeatService(this, serverSocket);
		nodeBalanceService = new NodeBalanceService(this);
		synchronizedGameActionHandler = new SynchronizedGameActionHandler(this);
		addMessageHandler(synchronizedGameActionHandler);
		addMessageHandler(heartbeatService);
		addMessageHandler(nodeBalanceService);
		addMessageHandler(syncServerState);
		serverSocket.logMessage("Server (" + getAddress() + ") is ready to join or create a cluster", LogType.INFO);
	}

	public void startCluster() throws ConnectionException {
		super.startCluster();
		// Setup battlefield
		nodeState = new ServerState(createBattlefield(), getAddress());
		startServices();
		serverSocket.logMessage("New cluster has been created", LogType.INFO);
	}

	public void connect(NodeAddress server) throws ConnectionException {
		try {
			super.connect(server);
			BattleField battlefield = SyncBattlefieldHandler.syncBattlefield(this);
			NodeState oldState = nodeState;
			nodeState = new ServerState(battlefield, getAddress());
			nodeState.getConnectedNodes().addAll(oldState.getConnectedNodes());
			startServices();
			heartbeatService.expectHeartbeatFrom(
					getConnectedNodes().stream().map(NodeState::getAddress).collect(toList()));
		} catch (ClusterException e) {
			serverSocket.logMessage("Could not connect server to cluster at " + server, LogType.ERROR);
		}
		serverSocket.logMessage("Connected server to the cluster of " + ownRegistry, LogType.INFO);
	}

	public void addServer(@NonNull NodeState server) {
		super.addServer(server);
		heartbeatService.expectHeartbeatFrom(server.getAddress());
	}

	public void addClient(@NonNull NodeAddress client) {
		getServerState().getClients().remove(client);
		getServerState().getClients().add(client);
		heartbeatService.expectHeartbeatFrom(client);
	}

	public void removeClient(@NonNull NodeAddress client) {
		getServerState().getClients().remove(client);
		getConnectedNodes().stream()
				.filter(c -> c.getAddress().getType().equals(NodeType.SERVER))
				.forEach(c -> ((ServerState) c).getClients().remove(client));
		try {
			socket.getRegistry().unbind(client.getName());
		}
		catch (RemoteException | NotBoundException e) {
			//e.printStackTrace();
		}
		heartbeatService.remove(client);
		getServerState().getBattleField().remove(client.getName());
	}

	public void moveClient(NodeAddress client, NodeAddress newServer) {
		getServerState().getClients().remove(client);
		nodeBalanceService.moveClientToServer(client, newServer);
	}

	public void updateOtherServerState(@NonNull ServerState that) {
		addServer(that);
		safeLogMessage("Updated connectedNodes: " + getConnectedNodes(), LogType.DEBUG);
	}

	private BattleField createBattlefield() {
		BattleField battlefield = new BattleField();
		battlefield.setServerSocket(serverSocket);
		battlefield.setMessagefactory(messageFactory);
		serverSocket.logMessage("Created the battlefield.", LogType.DEBUG);
		return battlefield;
	}

	private void startServices() {
		runService(heartbeatService);
		runService(syncServerState);
		runService(nodeBalanceService);
	}

	public void launchViewer() {
		if(getServerState() != null && getServerState().getBattleField() != null) {
			new BattleFieldViewer(getServerState().getBattleField());
		} else {
			safeLogMessage("Cannot launch battlefield-viewer; no battlefield available!", LogType.ERROR);
		}
	}

	public ServerState getServerState() {
		return (ServerState) nodeState;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.SERVER;
	}
}