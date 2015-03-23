package distributed.systems.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMessageReceivedHandler extends Remote {

	void onMessageReceived(Message message) throws RemoteException;

    //void onMessageReceivedinit(Message message) throws RemoteException;
}
