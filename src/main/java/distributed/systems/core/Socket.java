package distributed.systems.core;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Optional;

import distributed.systems.das.BattleField;
import distributed.systems.das.units.Unit;
import distributed.systems.example.LogHandler;
import distributed.systems.example.NodeAddress;
import distributed.systems.example.ServerNode;

public interface Socket {

	public void register(String id);

	public void addMessageReceivedHandler(IMessageReceivedHandler handler);

	public void sendMessage(Message reply, String origin);

	public void unRegister();

	public List<NodeAddress> getNodes() throws RemoteException;

	public NodeAddress determineAddress(NodeAddress.NodeType type) throws RemoteException;

	public Optional<NodeAddress> findServer() throws RemoteException;

}
