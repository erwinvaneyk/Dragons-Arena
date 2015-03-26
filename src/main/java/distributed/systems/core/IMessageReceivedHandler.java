package distributed.systems.core;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Optional;

public interface IMessageReceivedHandler extends Remote {

	Message onMessageReceived(Message message) throws RemoteException;
}
