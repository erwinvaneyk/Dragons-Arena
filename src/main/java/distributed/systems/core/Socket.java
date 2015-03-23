package distributed.systems.core;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Optional;

import distributed.systems.network.Address;
import distributed.systems.network.NodeAddress;

public interface Socket {

	public static final int MILISECOND_TIMEOUT = 3000;

	public static void waitUntillTimeout() throws RemoteException {
		int interval = 100;
		try {
			while() {
				
				Thread.sleep(interval);
			}
		}
		catch (InterruptedException e) {}
		throw new RemoteException("Time out occurred!");
	}

	public void register(String id);

	public void addMessageReceivedHandler(IMessageReceivedHandler handler);

	public void sendMessage(Message message, String origin);

	public void logMessage(Message logMessage);

	public void logMessage(String message, LogType type);

	public void unRegister();

	public List<NodeAddress> getNodes() throws RemoteException;

	public NodeAddress determineAddress(NodeAddress.NodeType type) throws RemoteException;

	public Optional<NodeAddress> findServer() throws RemoteException;

	public void broadcast(Message message, NodeAddress.NodeType type) throws RemoteException;

	public void broadcast(Message message) throws RemoteException;

	public Address getAddress();
}
