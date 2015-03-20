package distributed.systems.example;

import static java.util.stream.Collectors.toList;

import javax.swing.text.html.Option;
import javax.xml.soap.Node;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.core.exception.AlreadyAssignedIDException;

public class LocalSocket implements Socket,Serializable {

	private String id;

	private Registry registry;

	private static final String PROTOCOL = "localsocket://";

	/**
	 * Creates a socket connected to the default server, ip 127.0.0.1 and port 1234
	 */
	public static LocalSocket connectToDefault() {
		return connectTo("127.0.0.1", RegistryNode.PORT);
	}

	public static LocalSocket connectTo(String ip, int port) {
		return new LocalSocket(ip, port);
	}

	protected LocalSocket(String ip, int port) {
		try {
			registry = LocateRegistry.getRegistry(ip, port);
			System.out.println(Arrays.toString(registry.list()));
		} catch (RemoteException e) {
			throw new RuntimeException("Could not connect LocalSocket to registry!", e);
		}
	}

	@Override
	public void register(String id) {
		this.id = id;
	}

	@Override
	public void addMessageReceivedHandler(IMessageReceivedHandler handler) {
		try {
			registry.bind(id, handler);
			System.out.println("Currently bound objects: " + Arrays.toString(registry.list()));
		}
		catch (AlreadyBoundException e) {
			throw new AlreadyAssignedIDException();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendMessage(Message message, String destination) {

		try {
			message.setOriginId(this.id);
			message.put("origin", PROTOCOL + this.id);
			IMessageReceivedHandler handler = (IMessageReceivedHandler) registry.lookup(destination.substring(PROTOCOL.length()));
			System.out.println(message);
			handler.onMessageReceived(message);
			System.out.println("send: " + message);
		}
		catch (NotBoundException | RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unRegister() {
		try {
			registry.unbind(id);
			System.out.println("Unregistered binding " + id);
		}
		catch (RemoteException | NotBoundException e) {
			System.out.println(id + " was already unRegistered!");
		}
	}

	@Override
	public List<NodeAddress> getNodes() throws RemoteException {
		return Arrays
				.stream(registry.list())
				.map(NodeAddress::fromAddress)
				.collect(toList());
	}

	public NodeAddress determineAddress(NodeAddress.NodeType type) throws RemoteException {
		int highestId = getNodes()
				.stream()
				.filter(node -> node.getType().equals(type))
				.mapToInt(NodeAddress::getId)
				.max().orElse(-1);
		System.out.println(highestId);
		return new NodeAddress(type, highestId + 1);
	}

	// Load Balancer
	public Optional<NodeAddress> findServer() throws RemoteException {
		return getNodes()
				.stream()
				.filter(NodeAddress::isServer)
				.findAny();
	}
}
