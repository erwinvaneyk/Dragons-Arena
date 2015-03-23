package distributed.systems.network;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.core.SynchronizedSocket;
import distributed.systems.das.BattleField;
import distributed.systems.das.presentation.BattleFieldViewer;

/**
 * A single server node
 *
 * Will also need to take care of the dragons (with fault tolerance)
 */
public class ServerNode extends UnicastRemoteObject implements Serializable, IMessageReceivedHandler {

	private transient final Socket socket;

	private transient final Registry registry;

	private BattleField battlefield;

	private NodeAddress address;

	private final Map<String,Socket> otherServers = new HashMap<>();

	public static void main(String[] args) throws RemoteException {
		new ServerNode();
	}

	public ServerNode() throws RemoteException{
		// Set own registry
		registry = new RegistryNode(RegistryNode.PORT).getRegistry();

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
	public void onMessageReceived(Message message) throws RemoteException {
		message.setReceivedTimestamp();
		switch(message.getMessageType()) {
			case HANDSHAKE:
				if(message.get("response") == null) {
					onHandshake(message);
				}
				break;
			case DISCOVER:
				if(message.get("response") == null) {
					onDiscover(message);
				}

			case GENERIC:
				// Battlefield-specific messages
				this.battlefield.onMessageReceived(message);
				break;
		}
	}

	/**
	 * Sender:      New Server
	 * Receiver:    Existing server in cluster
	 * Result:      server is added
	 *
	 * Message to cluster:
	 * - MessageType: HANDSHAKE
	 * - serverId:  id of server added
	 * - address:   address of the new server
	 *
	 * Return message
	 * - MessageType: HANDSHAKE
	 * - response: true
	 * - success: true/false
	 *
	 */
	private void onHandshake(Message message) {
		String newServerId = (String) message.get("serverId");
		Address newServerAddress = (Address) message.get("address");
		Socket newServerSocket = LocalSocket.connectTo(newServerAddress);
		otherServers.put(newServerId, newServerSocket);
	}

	public void handshake(Address hostAddress) {
		Socket socket = LocalSocket.connectTo(hostAddress);
		socket.sendMessage(new Message(Message.Type.HANDSHAKE).put("serverId", address.toString()).put("address", hostAddress), address.toString());
	}

	/**
	 * Get all known servers in a cluster
	 *
	 * Message:
	 * - MessageType: DISCOVER
	 *
	 * Return message:
	 * - MessageType: DISCOVER
	 * - response: true
	 * - servers: Map<String, Address> all servers known to this node
	 */
	private void onDiscover(Message message) {
		HashMap<String, Address> servers = new HashMap<>();
		for(Map.Entry<String, Socket> server : otherServers.entrySet()) {
			servers.put(server.getKey(), server.getValue().getAddress());
		}
		otherServers.get(message.getOriginId()).sendMessage(
				new Message(Message.Type.DISCOVER).put("servers", servers).put("response", true), message.getOriginId());
	}
}