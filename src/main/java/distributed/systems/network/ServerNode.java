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
public class ServerNode extends AbstractServerNode implements IMessageReceivedHandler {

	@Getter
	private BattleField battlefield;

    private HeartbeatService heartbeatService;
	private NodeBalanceService nodeBalanceService;


	public static void main(String[] args) throws RemoteException {
		new ServerNode(RegistryNode.PORT);
	}

	public ServerNode(int port) throws RemoteException {
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
		serverSocket.logMessage("Starting new cluster, starting with id 0", LogType.DEBUG);
		address.setId(0);
		updateBindings();
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
}