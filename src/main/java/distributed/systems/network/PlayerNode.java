package distributed.systems.network;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.das.MessageRequest;
import distributed.systems.das.units.Player;
import distributed.systems.das.units.Unit;
import distributed.systems.das.units.impl.RandomPlayer;
import distributed.systems.das.units.impl.SimplePlayer;
import distributed.systems.network.messagehandlers.ClientGameActionHandler;
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

public class PlayerNode extends AbstractNode implements ClientNode, IMessageReceivedHandler {

	private final ClientGameActionHandler clientGameActionHandler;
	private transient Player player;

	private final PlayerState playerState;

	private transient List<NodeAddress> knownServers = new ArrayList<>();

	private transient HeartbeatService heartbeatService;

	public static void main(String[] args) throws RemoteException {
		new PlayerNode(null, 1, 1);
	}

	public NodeAddress getServerAddress() {
		return playerState.getServerAddress();
	}

	public NodeAddress getAddress() {
		return playerState.getAddress();
	}

	public PlayerNode(NodeAddress server, int x, int y) throws RemoteException {
		// Setup
		NodeAddress address = new NodeAddress(-1, getNodeType());
		playerState = new PlayerState(address, server);
		messageFactory = new MessageFactory(playerState);

		joinServer(server);

		// Add message handlers
		addMessageHandler(heartbeatService);
		clientGameActionHandler = new ClientGameActionHandler(this);
		addMessageHandler(clientGameActionHandler);

		// TODO: get reserve servers

		// Setup heartbeat service
		runService(heartbeatService);

		// spawn player
		this.player = new SimplePlayer(x,y, this);
		System.out.println("Outputting logs to the associated server: " + playerState.getServerAddress());
		socket.logMessage("Player (" + address + ") created and running. Assigned to server: " + playerState.getServerAddress(), LogType.INFO);
		player.start();
	}

	public PlayerNode(NodeAddress server) throws RemoteException {
		// Setup
		NodeAddress address = new NodeAddress(-1, getNodeType());
		playerState = new PlayerState(address, server);
		messageFactory = new MessageFactory(playerState);

		joinServer(server);

		// Add message handlers
		addMessageHandler(heartbeatService);
		clientGameActionHandler = new ClientGameActionHandler(this);
		addMessageHandler(clientGameActionHandler);

		// TODO: get reserve servers

		// Setup heartbeat service
		runService(heartbeatService);
		player = spawn();

		System.out.println("Outputting logs to the associated server: " + playerState.getServerAddress());
		socket.logMessage("Player (" + address + ") created and running. Assigned to server: " + playerState.getServerAddress(), LogType.INFO);
		player.start();
	}

	public Player spawn() {
		int id = clientGameActionHandler.nextId();
		Message message = messageFactory.createMessage().put("request", MessageRequest.getFreeLocation).put("id", id);
		socket.sendMessage(message, playerState.getServerAddress());
		Message response = clientGameActionHandler.waitAndGetMessage(id);
		boolean hasFreeSpot = (Boolean) response.get("success");
		if(!hasFreeSpot) {
			throw new ClusterException("Player " + playerState.getAddress() + " cannot join server; server is full");
		}
		int x = (Integer) response.get("x");
		int y = (Integer) response.get("y");
		safeLogMessage("Found spot: (" + x + ", " + y + ")",LogType.DEBUG);
		// spawn player
		return new SimplePlayer(x,y, this);
	}

	public void joinServer(NodeAddress server) {
		// Join server
		NodeAddress serverAddress = NodeBalanceService.joinServer(this, server);
		playerState.setServerAddress(serverAddress);
		socket = LocalSocket.connectTo(serverAddress);
		socket.register(playerState.getAddress());
		socket.addMessageReceivedHandler(this);
		heartbeatService = new ClientHeartbeatService(this, socket).expectHeartbeatFrom(serverAddress);
		socket.logMessage("Dragon (" + playerState.getAddress() + ") joined server: " + playerState.getServerAddress(), LogType.INFO);
	}

	@Override
	public Unit getUnit() {
		return player;

	}

	@Override
	public Message sendMessageToServer(Message message) {
		return socket.sendMessage(message, this.getServerAddress());
	}

	@Override
	public PlayerState getPlayerState() {
		return playerState;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.PLAYER;
	}
}
