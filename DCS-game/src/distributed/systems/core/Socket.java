package distributed.systems.core;

import distributed.systems.das.BattleField;
import distributed.systems.das.units.Unit;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Socket extends Remote {

	public void register(String serverid) throws RemoteException;

	public void addMessageReceivedHandler(BattleField battleField) throws RemoteException;

	public void addMessageReceivedHandler(Unit unit) throws RemoteException;

	public void sendMessage(Message reply, String origin) throws RemoteException;

	public void unRegister() throws RemoteException;

}
