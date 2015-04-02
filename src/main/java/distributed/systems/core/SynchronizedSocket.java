package distributed.systems.core;

import distributed.systems.network.ConnectionException;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeType;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

public class SynchronizedSocket implements ExtendedSocket,Serializable{

	private final ExtendedSocket local;

	public SynchronizedSocket(ExtendedSocket local) {
		this.local = local;
	}

	@Override
	public void register(NodeAddress serverid) {
		this.local.register(serverid);
	}

	@Override
	public void addMessageReceivedHandler(IMessageReceivedHandler handler) {
		this.local.addMessageReceivedHandler(handler);
	}


	@Override
	public Message sendMessage(Message message, NodeAddress destination) throws ConnectionException {
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
	public void broadcast(Message message, NodeType type) {
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
