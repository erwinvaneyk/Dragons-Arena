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

		// setup services
		heartbeatService = new ServerHeartbeatService(this, serverSocket).expectHeartbeatFrom(otherNodes);
		nodeBalanceService = new NodeBalanceService(this);
		addMessageHandler(heartbeatService);
		addMessageHandler(nodeBalanceService);
		addMessageHandler(new ServerGameActionHandler(this));
		runService(heartbeatService);

		// TODO: start a dragon (if necessary)
		serverSocket.logMessage("Server (" + address + ") is up and running", LogType.INFO);
	}

	public void startCluster() {
		super.startCluster();
		// Setup battlefield
		battlefield = new BattleField();
		battlefield.setServerSocket(socket);
		battlefield.setMessagefactory(messageFactory);
		serverSocket.logMessage("Created the battlefield.", LogType.DEBUG);
	}

	public void connect(NodeAddress server) {
		super.connect(server);
		battlefield = SyncBattlefieldHandler.syncBattlefield(this);
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