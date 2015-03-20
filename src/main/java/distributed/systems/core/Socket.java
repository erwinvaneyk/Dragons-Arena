package distributed.systems.core;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.List;

import distributed.systems.das.BattleField;
import distributed.systems.das.units.Unit;
import distributed.systems.example.LogHandler;
import distributed.systems.example.NodeAddress;
import distributed.systems.example.ServerNode;

public interface Socket {

	public void register(String id);

	public void addMessageReceivedHandler(BattleField battleField);

	public void addMessageReceivedHandler(Unit unit);

	public void addMessageReceivedHandler(ServerNode logger);

	public void addLoggingReceivedHandler(LogHandler logger);

	public void sendMessage(Message reply, String origin);

	public void unRegister();

	public List<NodeAddress> getNodes() throws RemoteException;

}
