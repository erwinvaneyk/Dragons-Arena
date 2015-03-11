package distributed.systems.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMessageReceivedHandler {
    public void onMessageReceived(Message message);
}
