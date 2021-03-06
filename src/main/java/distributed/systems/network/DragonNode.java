package distributed.systems.network;


import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.das.MessageRequest;
import distributed.systems.das.units.Dragon;
import distributed.systems.das.units.Player;
import distributed.systems.das.units.Unit;
import distributed.systems.das.units.impl.SimplePlayer;
import distributed.systems.das.units.impl.StationaryDragon;
import distributed.systems.network.messagehandlers.ClientGameActionHandler;
import distributed.systems.network.messagehandlers.ClientNodeBalanceHandler;
import distributed.systems.network.messagehandlers.ClientServerSyncHandler;
import distributed.systems.network.services.ClientHeartbeatService;
import distributed.systems.network.services.HeartbeatService;
import distributed.systems.network.services.NodeBalanceService;

import lombok.Getter;
import lombok.Setter;


public class DragonNode extends AbstractClientNode {

	private final PlayerState playerState;
	private final ClientGameActionHandler clientGameActionHandler;

	@Getter
	private HeartbeatService heartbeatService;
	private Dragon dragon;

	public static void main(String[] args) throws RemoteException, ConnectionException {
		new DragonNode(null, 1, 1);
	}

	public DragonNode(NodeAddress server, int x, int y) throws RemoteException, ConnectionException {
		// Setup
		NodeAddress address = new NodeAddress(-1, NodeType.DRAGON);
		playerState = new PlayerState(address, server);
		messageFactory = new MessageFactory(new NodeState(address));

		// Join server
		joinServer(server);

		// Add message handlers
		addMessageHandler(heartbeatService);
		clientGameActionHandler = new ClientGameActionHandler(this);
		addMessageHandler(clientGameActionHandler);
		addMessageHandler(new ClientNodeBalanceHandler(this));
		addMessageHandler(new ClientServerSyncHandler(this));

		// TODO: get reserve servers

		// Setup heartbeat service
		runService(heartbeatService);

		// spawn dragon

		this.dragon = new StationaryDragon((Integer) x,y, this);
		socket.logMessage("Dragon (" + playerState.getAddress() + ") created and running. Assigned to server: " + playerState.getServerAddress(), LogType.INFO);
		dragon.start();
	}

	public DragonNode(NodeAddress server) throws RemoteException, ConnectionException {
		// Setup
		NodeAddress address = new NodeAddress(-1, NodeType.DRAGON);
		playerState = new PlayerState(address, server);
		messageFactory = new MessageFactory(new NodeState(address));

		// Join server
		joinServer(server);

		// Add message handlers
		addMessageHandler(heartbeatService);
		clientGameActionHandler = new ClientGameActionHandler(this);
		addMessageHandler(clientGameActionHandler);
		addMessageHandler(new ClientNodeBalanceHandler(this));
		addMessageHandler(new ClientServerSyncHandler(this));

		// TODO: get reserve servers

		// Setup heartbeat service
		runService(heartbeatService);

		// spawn dragon

		this.dragon = spawn();
		socket.logMessage("Dragon (" + playerState.getAddress() + ") created and running. Assigned to server: " + playerState.getServerAddress(), LogType.INFO);
		dragon.start();
	}


	public StationaryDragon spawn() {
		int id = clientGameActionHandler.nextId();
		Message message = messageFactory.createMessage().put("request", MessageRequest.getFreeLocation).put("id", id);
		socket.sendMessage(message, playerState.getServerAddress());
		Message response = clientGameActionHandler.waitAndGetMessage(id);
		boolean hasFreeSpot = (Boolean) response.get("success");
		if(!hasFreeSpot) {
			throw new ClusterException("Player " + playerState.getAddress() + " cannot join server; server is full");
		}
		// spawn player
		return new StationaryDragon((Integer) response.get("x"),(Integer) response.get("y"), this);
	}

	public void joinServer(NodeAddress server) throws ConnectionException {
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
	public NodeAddress getServerAddress() {
		return playerState.getServerAddress();
	}

	@Override
	public NodeAddress getAddress() {
		return playerState.getAddress();
	}

	@Override
	public Unit getUnit() {
		return dragon;
	}

	@Override
	public PlayerState getPlayerState() {
		return playerState;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.DRAGON;
	}
}
