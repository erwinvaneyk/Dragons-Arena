package distributed.systems.network;

import java.rmi.NoSuchObjectException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.core.Socket;
import distributed.systems.das.units.Player;
import distributed.systems.network.services.HeartbeatService;
import lombok.Getter;

/**
 * Requirements for a player/dragon/unit:
 * - ServerID
 * - MAP_WIDTH, MAP_HEIGHT
 * - playerID (to generate it, only one that calls the battlefield)
 * - Socket
 */
public class PlayerNode extends UnicastRemoteObject implements ClientNode, IMessageReceivedHandler {
	private final ExecutorService services = Executors.newCachedThreadPool();

	@Getter
	private ExtendedSocket socket;
	private MessageFactory messageFactory;

	private Player player;

	@Getter
	private NodeAddress address;
	private List<ServerAddress> knownServers = new ArrayList<>();

	@Getter
	private NodeAddress serverAddress;
	private HeartbeatService heartbeatService;

	public static void main(String[] args) throws RemoteException {
		new PlayerNode(null, 1, 1);
	}

	public PlayerNode(NodeAddress server, int x, int y) throws RemoteException {
		setup(server);
		// Connect to cluster

		// spawn player
		this.player = new Player(x,y, this);
		socket.logMessage("Player (" + address + ") created and running. Assigned to server: " + serverAddress, LogType.INFO);
		player.start();
	}

	public void setup(NodeAddress server) {
		// Setup
		address = new NodeAddress(-1, NodeAddress.NodeType.PLAYER);
		serverAddress = server;
		messageFactory = new MessageFactory(address);

		// Join server
		serverAddress = joinServer(serverAddress);
		knownServers.add(new ServerAddress(serverAddress));


		// TODO: get reserve servers

		// Setup heartbeat service
		heartbeatService = new HeartbeatService(knownServers, socket, messageFactory);
		services.submit(heartbeatService);
	}

	private NodeAddress joinServer(NodeAddress serverAddress) {
		// Connect to cluster (initially before being rebalanced)
		socket = LocalSocket.connectTo(serverAddress);
		// Acknowledge
		Message response = socket.sendMessage(messageFactory.createMessage(Message.Type.JOIN_SERVER), serverAddress);
		if(response.get("redirect") != null) {
			NodeAddress redirectedAddress = (NodeAddress) response.get("redirect");
			// Attempt to join the other server
			socket.logMessage("Being redirected from `" + serverAddress + "` to `" + redirectedAddress + "`", LogType.INFO);
			return joinServer(redirectedAddress);
		} else {
			// Join this server
			NodeAddress myNewAddress = (NodeAddress) response.get("address");
			serverAddress = (ServerAddress) response.get("server");
			socket = LocalSocket.connectTo(serverAddress);
			address.setId(myNewAddress.getId());
			address.setPhysicalAddress(myNewAddress.getPhysicalAddress());
			socket.register(address);
			socket.addMessageReceivedHandler(this);
			socket.logMessage("Added the player `" + address +"` to server `" + serverAddress + "`", LogType.INFO);
		}
		return serverAddress;
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		message.setReceivedTimestamp();
		switch (message.getMessageType()) {
			case GENERIC:
				socket.logMessage("[" + address + "] received message: (" + message + ")", LogType.DEBUG);
				this.player.onMessageReceived(message);
				break;
			case HEARTBEAT:
				return heartbeatService.onMessageReceived(message);
		}
		return messageFactory.createReply(message).setMessageType(Message.Type.ACK);
	}

	public void disconnect() {
		// Unregister the API
		socket.unRegister();

		// Stop services
		services.shutdownNow();

		try {
			// Stop exporting this object
			UnicastRemoteObject.unexportObject(this, true);
			socket.logMessage("Disconnected `" + address + "`.", LogType.INFO);
		}
		catch (NoSuchObjectException e) {
			e.printStackTrace();
		}
	}
}
