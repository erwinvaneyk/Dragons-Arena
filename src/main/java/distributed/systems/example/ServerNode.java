package distributed.systems.example;

import static java.util.stream.Collectors.toList;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.List;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.core.SynchronizedSocket;
import distributed.systems.das.BattleField;

/**
 * A single server node
 *
 * Will also need to take care of the dragons (with fault tolerance)
 */
public class ServerNode extends UnicastRemoteObject implements IMessageReceivedHandler {

	private final static String REGISTRY_PREFIX = "server";

	private final Socket serverSocket;

	private BattleField battlefield;

	private NodeAddress address;

	//private final BattleField battlefield;

	public static void main(String[] args) throws RemoteException {
		new ServerNode();
	}

	public ServerNode() throws RemoteException{
		// TODO: register to cluster
		serverSocket = connectToCluster();
		// TODO: Acknowledge network/handshake
		// TODO: sync with network
		// TODO: Setup battlefield (self or from network)
		battlefield = BattleField.getBattleField();
		battlefield.setServerSocket(serverSocket);
		// TODO: start a dragon (if necessary)
	}

	private Socket connectToCluster() throws RemoteException {
		Socket socket = new SynchronizedSocket(LocalSocket.connectToDefault());
		address = determineAddress(socket);
		socket.register(address.toString());
		socket.addMessageReceivedHandler(this);
		return socket;
	}

	private NodeAddress determineAddress(Socket socket) throws RemoteException {
		int highestId = socket.getNodes()
				.stream()
				.filter(node -> node.getType().equals(NodeAddress.NodeType.SERVER))
				.mapToInt(NodeAddress::getId)
				.max().orElse(-1);
		return new NodeAddress(NodeAddress.NodeType.SERVER, highestId + 1);
	}


	@Override
	public void onMessageReceived(Message message) {

	}
}