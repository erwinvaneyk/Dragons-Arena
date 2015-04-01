package distributed.systems.network;

import distributed.systems.core.*;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.MessageRequest;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 *
 * Socket serving as an interface for single RMIRegistry
 *
 */
@ToString
public class LocalSocket implements ExtendedSocket,Serializable {

	private NodeAddress id;

	@Getter
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
	}

	@Override
	public void addMessageReceivedHandler(IMessageReceivedHandler handler) {
		try {
			registry.bind(id.getName(), handler);
			logMessage("Bounded " + id.getName() + " on registry of " + registryAddress.getPhysicalAddress() + ". Now contains: " + Arrays.toString(registry.list()), LogType.DEBUG);
		}
		catch (AlreadyBoundException e) {
			throw new AlreadyAssignedIDException("ID: " + id + " was already assigned!", e);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private Optional<IMessageReceivedHandler> lookup(String binding) {
		try {
			return Optional.ofNullable((IMessageReceivedHandler) registry.lookup(binding));
		} catch (NotBoundException | RemoteException e) {
			return Optional.empty();
		}
	}

	@Override
	public Message sendMessage(Message message, NodeAddress destination) {
		Optional<IMessageReceivedHandler> handler = lookup(destination.getName());
		if(id != null) {
			message.setOrigin(id);
		}
		if(!handler.isPresent()) {
			String errorMessage = "Attempting to send message to non-registered node: " + destination;
			try {
				errorMessage += " on " + Arrays.toString(registry.list());
			} catch (RemoteException e) {
				errorMessage += " on unknown registry.";
			}
			throw new ClusterException(errorMessage + " with message: \"" + message + "\"");
		} else {
			try {
				return handler.get().onMessageReceived(message);
			} catch (RemoteException e) {
				throw new RuntimeException("Failed to send message: `" + message + "` to " + destination, e);
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

	@Override
	public void logMessage(Message logMessage) {
		try {
			sendMessage(logMessage, registryAddress);
		} catch (Exception e) {
			System.out.println("No registry-server present: " + logMessage);
			e.printStackTrace();
		}
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

	@Deprecated
	public void broadcast(Message message, NodeType type) {
		try {
			getNodes().stream()
					.filter(address -> address.getType().equals(type))
					.forEach(address -> sendMessage(message, address));
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
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
