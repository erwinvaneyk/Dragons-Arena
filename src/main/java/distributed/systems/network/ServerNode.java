package distributed.systems.network;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.core.SynchronizedSocket;
import distributed.systems.das.BattleField;
import distributed.systems.das.presentation.BattleFieldViewer;
import lombok.Getter;

/**
 * A single server node
 *
 * Will also need to take care of the dragons (with fault tolerance)
 */
public class ServerNode extends UnicastRemoteObject implements IMessageReceivedHandler {

	private final static String REGISTRY_PREFIX = "server";

	private final Socket socket;

    @Getter
	private BattleField battlefield;

	private NodeAddress address;

	public static void main(String[] args) throws RemoteException {
		new ServerNode();
	}

	public ServerNode() throws RemoteException{
		// TODO: register to cluster
		socket = connectToCluster();
		// TODO: Acknowledge network/handshake
		// TODO: sync with network
		// TODO: Setup battlefield (self or from network)
		battlefield = BattleField.getBattleField();
		battlefield.setServerSocket(socket);
		// TODO: start a dragon (if necessary)

		socket.logMessage("Server (" + address + ") is up and running", LogType.INFO);
		/* Spawn a new battlefield viewer */
		new Thread(BattleFieldViewer::new).start();
	}

	private Socket connectToCluster() throws RemoteException {
		Socket socket = new SynchronizedSocket(LocalSocket.connectToDefault());
		address = socket.determineAddress(NodeAddress.NodeType.SERVER);
		socket.register(address.toString());
		socket.addMessageReceivedHandler(this);
		return socket;
	}


	@Override
	public void onMessageReceived(Message message) throws RemoteException{
		message.setReceivedTimestamp();
		// TODO: handle other messages

		// Battlefield-specific messages
        //System.out.println("serverNode onMessageReceived"+message.toString());
		this.battlefield.onMessageReceived(message);
	}
}