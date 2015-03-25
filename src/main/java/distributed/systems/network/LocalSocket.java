package distributed.systems.network;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.LogMessage;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import lombok.ToString;

/**
 *
 * Socket serving as an interface for single RMIRegistry
 *
 */
@ToString
public class LocalSocket implements ExtendedSocket,Serializable {

	private NodeAddress id;

	private final Registry registry;

	private final NodeAddress registryAddress;

	private transient MessageFactory messageFactory;

	/**
	 * Creates a socket connected to the default server, ip 127.0.0.1 and port 1234
	 */
	public static LocalSocket connectToDefault() {
		return connectTo("127.0.0.1", RegistryNode.PORT);
	}

	public static LocalSocket connectTo(String ip, int port) {
		return new LocalSocket(ip, port);
	}

	public static LocalSocket connectTo(NodeAddress address) {
		return new LocalSocket(address);
	}

	protected LocalSocket(String ip, int port) {
		try {
			registry = LocateRegistry.getRegistry(ip, port);
			registryAddress = new NodeAddress(ip, port);
		} catch (RemoteException e) {
			throw new RuntimeException("Could not connect LocalSocket to registry!", e);
		}
	}

	protected LocalSocket(NodeAddress address) {
		try {
			registry = LocateRegistry.getRegistry(address.getPhysicalAddress().getIp(), address.getPhysicalAddress().getPort());
			registryAddress = address;
		} catch (RemoteException e) {
			throw new RuntimeException("Could not connect LocalSocket to registry!", e);
		}
	}

	@Override
	public void register(NodeAddress id) {
		this.id = id;
		this.messageFactory = new MessageFactory(id);
	}

	@Override
	public void addMessageReceivedHandler(IMessageReceivedHandler handler) {
		try {
			registry.bind(id.getName(), handler);
			logMessage("Bounded " + id.getName() + " on registry of " + registryAddress + ". Now contains: " + Arrays.toString(registry.list()), LogType.DEBUG);
		}
		catch (AlreadyBoundException e) {
			throw new AlreadyAssignedIDException();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Message sendMessage(Message message, NodeAddress destination) {
		try {
			if(id != null) message.setOrigin(id);
			//message.put("origin", PROTOCOL_LOCAL + PROTOCOL_SEPARATOR + this.id);
			IMessageReceivedHandler handler = (IMessageReceivedHandler) registry.lookup(destination.getName());
			return handler.onMessageReceived(message);
		}
		catch (NotBoundException | RemoteException e) {
			try {
				e.printStackTrace();
				throw new RuntimeException("Failed to send message: `" + message + "` to " + destination + " on registry "+ Arrays.toString(registry.list()), e);
			}
			catch (RemoteException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	@Override
	public void unRegister() {
		try {
			registry.unbind(id.getName());
		}
		catch (RemoteException | NotBoundException e) {
			System.out.println(id + " was already unRegistered!");
		}
	}

	// TODO: seperate this to serversocket

	@Override
	public void logMessage(Message logMessage) {
		sendMessage(logMessage, registryAddress);
	}

	@Override
	public void logMessage(String message, LogType type) {
		LogMessage log;
		if(messageFactory != null) {
			log = messageFactory.createLogMessage(message, type);
		} else {
			log = new LogMessage(message, type);

		}
		logMessage(log);
	}


	@Override
	public List<NodeAddress> getNodes() throws RemoteException {
		return Arrays
				.stream(registry.list())
				.map(NodeAddress::fromAddress)
				.collect(toList());
	}

	@Override
	public NodeAddress determineAddress(NodeAddress.NodeType type) throws RemoteException {
		return null;
	}

	// Load Balancer
	public Optional<NodeAddress> findServer() throws RemoteException {
		return getNodes()
				.stream()
				.filter(NodeAddress::isServer)
				.findAny();
	}

	public void broadcast(Message message, NodeAddress.NodeType type) {
		try {
			getNodes().stream()
					.filter(address -> address.getType().equals(type))
					.forEach(address -> sendMessage(message, address));
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void broadcast(Message message) {
		try {
			getNodes().stream().forEach(address -> sendMessage(message, address));
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public NodeAddress getRegistryAddress() {
		return registryAddress;
	}
}
