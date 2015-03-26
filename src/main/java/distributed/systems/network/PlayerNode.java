package distributed.systems.network;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.LogType;
import distributed.systems.core.MessageFactory;
import distributed.systems.das.units.Player;
import distributed.systems.das.units.Unit;
import distributed.systems.network.messagehandlers.ClientGameActionHandler;
import distributed.systems.network.messagehandlers.ServerGameActionHandler;
import distributed.systems.network.services.ClientHeartbeatService;
import distributed.systems.network.services.HeartbeatService;
import distributed.systems.network.services.NodeBalanceService;
import lombok.Getter;

/**
 * Requirements for a player/dragon/unit:
 * - ServerID
 * - MAP_WIDTH, MAP_HEIGHT
 * - playerID (to generate it, only one that calls the battlefield)
 * - Socket
 */
public class PlayerNode extends BasicNode implements ClientNode, IMessageReceivedHandler {

	private Player player;

	private List<ServerAddress> knownServers = new ArrayList<>();

	@Getter
	private NodeAddress serverAddress;
	private HeartbeatService heartbeatService;

	public static void main(String[] args) throws RemoteException {
		new PlayerNode(null, 1, 1);
	}

	public PlayerNode(NodeAddress server, int x, int y) throws RemoteException {
		// Setup
		address = new NodeAddress(-1, NodeAddress.NodeType.PLAYER);
		serverAddress = server;
		messageFactory = new MessageFactory(address);

		// Join server
		serverAddress = NodeBalanceService.joinServer(this, serverAddress);
		socket = LocalSocket.connectTo(serverAddress);
		socket.register(address);
		socket.addMessageReceivedHandler(this);
		knownServers.add(new ServerAddress(serverAddress));
		heartbeatService = new ClientHeartbeatService(this, socket, knownServers);

		// Add message handlers
		addMessageHandler(heartbeatService);
		addMessageHandler(new ClientGameActionHandler(this));

		// TODO: get reserve servers

		// Setup heartbeat service
		runService(heartbeatService);

		// spawn player
		this.player = new Player(x,y, this);
		socket.logMessage("Player (" + address + ") created and running. Assigned to server: " + serverAddress, LogType.INFO);
		player.start();
	}

	@Override
	public Unit getUnit() {
		return player;
	}
}
