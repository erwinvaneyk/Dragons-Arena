package distributed.systems.network;

import java.rmi.NoSuchObjectException;
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
import distributed.systems.core.SynchronizedSocket;
import distributed.systems.das.units.Dragon;
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
public class DragonNode extends UnicastRemoteObject implements ClientNode, IMessageReceivedHandler {

    private final ExecutorService services = Executors.newCachedThreadPool();

	@Getter
	private ExtendedSocket socket;
	private MessageFactory messageFactory;

	private Dragon dragon;

	@Getter
	private NodeAddress address;
    private List<NodeAddress> knownServers = new ArrayList<>();

	@Getter
	private NodeAddress serverAddress;
    private HeartbeatService heartbeatService;

	public static void main(String[] args) throws RemoteException {
		new DragonNode(null, 1, 1);
	}

/*	public DragonNode(int x, int y) throws RemoteException {
		// Connect to cluster
		socket = new SynchronizedSocket(LocalSocket.connectToDefault());
		address = socket.determineAddress(NodeAddress.NodeType.DRAGON);
		socket.register(address.toString());
		socket.addMessageReceivedHandler(this);
		messageFactory = new MessageFactory(address);
		// find suitable server
		// TODO: needs to be load balanced
		serverAddress = socket.findServer().orElseThrow(() -> new RuntimeException("No server available"));
		// spawn player
		this.dragon = new Dragon(x,y, this);
		socket.logMessage("Dragon (" + address + ") created and running. Assigned to server: " + serverAddress, LogType.INFO);
		dragon.start();
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		message.setReceivedTimestamp();
		this.dragon.onMessageReceived(message);
		return messageFactory.createReply(message).setMessageType(Message.Type.ACK);
	}*/
    public DragonNode(NodeAddress server, int x, int y) throws RemoteException {
        setup(server);
        // Connect to cluster

        // spawn player
        this.dragon = new Dragon(x,y, this);
        socket.logMessage("Dragon (" + address + ") created and running. Assigned to server: " + serverAddress, LogType.INFO);
        dragon.start();
    }

    public void setup(NodeAddress server) {
        // Setup
        address = new NodeAddress(-1, NodeAddress.NodeType.PLAYER);
        serverAddress = server;
        messageFactory = new MessageFactory(address);

        // Join server
        serverAddress = joinServer(serverAddress);
        knownServers.add(serverAddress);


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
            NodeAddress newAddress = (NodeAddress) response.get("address");
            address.setId(newAddress.getId());
            address.setPhysicalAddress(newAddress.getPhysicalAddress());
            socket.register(address.getName());
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
                this.dragon.onMessageReceived(message);
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
