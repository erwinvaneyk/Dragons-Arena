package distributed.systems.network;

import java.rmi.RemoteException;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.LogType;
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
import lombok.NonNull;


/**
 * A single server node
 *
 * Will also need to take care of the dragons (with fault tolerance)
 */
@ToString
public class ServerNode extends AbstractServerNode implements IMessageReceivedHandler {

	@Getter
	private BattleField battlefield;

    private HeartbeatService heartbeatService;
	private NodeBalanceService nodeBalanceService;


	public static void main(String[] args) throws RemoteException {
		new ServerNode(RegistryNode.PORT);
	}

	public ServerNode(int port) throws RemoteException {
		super(port);
		// Setup message-handlers
		addMessageHandler(new LogHandler(this));
		addMessageHandler(new SyncBattlefieldHandler(this));
		addMessageHandler(new ServerJoinHandler(this));
		addMessageHandler(new ServerGameActionHandler(this));

		// setup services
		heartbeatService = new ServerHeartbeatService(this, serverSocket).expectHeartbeatFrom(otherNodes);
		nodeBalanceService = new NodeBalanceService(this);
		addMessageHandler(heartbeatService);
		addMessageHandler(nodeBalanceService);
		serverSocket.logMessage("Server (" + address + ") is ready to join or create a cluster", LogType.INFO);
	}

	public void startCluster() {
		super.startCluster();
		// Setup battlefield
		battlefield = createBattlefield();
		startServices();
		serverSocket.logMessage("New cluster has been created", LogType.INFO);
	}

	public void connect(NodeAddress server) {
		try {
			super.connect(server);
			battlefield = SyncBattlefieldHandler.syncBattlefield(this);
			startServices();
		} catch (RuntimeException e) {
			serverSocket.logMessage("Could not connect to cluster at " + server, LogType.ERROR);
		}
	}

	private BattleField createBattlefield() {
		BattleField battlefield = new BattleField();
		battlefield.setServerSocket(socket);
		battlefield.setMessagefactory(messageFactory);
		serverSocket.logMessage("Created the battlefield.", LogType.DEBUG);
		return battlefield;
	}

	private void startServices() {
		runService(heartbeatService);
	}

	public void launchViewer() {
		if(battlefield != null) {
			new BattleFieldViewer(battlefield);
		} else {
			safeLogMessage("Cannot launch battlefield-viewer; no battlefield available!", LogType.ERROR);
		}
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.SERVER;
	}
}