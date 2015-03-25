package distributed.systems.core;

import distributed.systems.network.NodeAddress;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

public class SynchronizedSocket implements ExtendedSocket,Serializable{

	private final ExtendedSocket local;

	public SynchronizedSocket(ExtendedSocket local) {
		this.local = local;
	}

	@Override
	public void register(String serverid) {
		this.local.register(serverid);
	}

	@Override
	public void addMessageReceivedHandler(IMessageReceivedHandler handler) {
		this.local.addMessageReceivedHandler(handler);
	}

	public Message sendMessage(Message reply, String origin) {
		return this.local.sendMessage(reply, origin);
	}

	@Override
	public Message sendMessage(Message message, NodeAddress destination) {
		return this.local.sendMessage(message, destination);
	}

	@Override
	public void logMessage(Message logMessage) {
		this.local.logMessage(logMessage);
	}

	@Override
	public void logMessage(String message, LogType type) {
		this.local.logMessage(message, type);
	}

	@Override
	public void unRegister() {
		this.local.unRegister();
	}

	@Override
	public List<NodeAddress> getNodes() throws RemoteException {
		return this.local.getNodes();
	}

	@Override
	public NodeAddress determineAddress(NodeAddress.NodeType type) throws RemoteException {
		return this.local.determineAddress(type);
	}

	@Override
	public Optional<NodeAddress> findServer() throws RemoteException {
		return this.local.findServer();
	}

	@Override
	public void broadcast(Message message, NodeAddress.NodeType type) {
		this.local.broadcast(message, type);
	}

	@Override
	public void broadcast(Message message) {
		this.local.broadcast(message);
	}

	@Override
	public NodeAddress getRegistryAddress() {
		return this.local.getRegistryAddress();
	}
}
