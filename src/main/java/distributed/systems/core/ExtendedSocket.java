package distributed.systems.core;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

import distributed.systems.network.NodeAddress;

public interface ExtendedSocket extends Socket {

	public void register(NodeAddress id);

	public void addMessageReceivedHandler(IMessageReceivedHandler handler);

	public void unRegister();

	public List<NodeAddress> getNodes() throws RemoteException;

	public NodeAddress getRegistryAddress();
}
