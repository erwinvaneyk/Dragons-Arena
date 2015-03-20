package distributed.systems.network;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.core.SynchronizedSocket;
import distributed.systems.das.units.Player;
import lombok.Getter;

/**
 * Requirements for a player/dragon/unit:
 * - ServerID
 * - MAP_WIDTH, MAP_HEIGHT
 * - playerID (to generate it, only one that calls the battlefield)
 * - Socket
 */
public class PlayerNode extends UnicastRemoteObject implements ClientNode, IMessageReceivedHandler {

	@Getter
	private final Socket socket;

	private Player player;

	@Getter
	private NodeAddress address;

	@Getter
	private NodeAddress serverAddress;

	public static void main(String[] args) throws RemoteException {
		new PlayerNode(1, 1);
	}

	public PlayerNode(int x, int y) throws RemoteException {
		// Connect to cluster
		socket = new SynchronizedSocket(LocalSocket.connectToDefault());
		address = socket.determineAddress(NodeAddress.NodeType.PLAYER);
		socket.register(address.toString());
		socket.addMessageReceivedHandler(this);
		System.out.println("Node is registered and bounded.");
		// find suitable server
		// TODO: needs to be load balanced
		serverAddress = socket.findServer().orElseThrow(() -> new RuntimeException("No server available"));
		System.out.println("Server was assigned:" + serverAddress);
		// spawn player
		this.player = new Player(x,y, this);
		System.out.println("Player created!");
		player.start();
		System.out.println("Player running...");
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException {
		message.setReceivedTimestamp();
		// TODO: if server/battlefield put message into queue
		this.player.onMessageReceived(message);
	}
}
