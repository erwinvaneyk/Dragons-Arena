package distributed.systems.core;

import distributed.systems.network.NodeAddress;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

public class SynchronizedSocket implements Socket,Serializable{

	private final Socket local;

	public SynchronizedSocket(Socket local) {
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

	public void sendMessage(Message reply, String origin) {
		this.local.sendMessage(reply, origin);
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
}
