package distributed.systems.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMessageProxyHandler extends Remote {

	void onMessageReceived(Message message) throws RemoteException;
}
