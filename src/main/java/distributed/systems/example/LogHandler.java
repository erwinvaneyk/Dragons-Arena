package distributed.systems.example;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import distributed.systems.core.IMessageProxyHandler;
import distributed.systems.core.Message;

public class LogHandler extends UnicastRemoteObject implements IMessageProxyHandler, Serializable {

	protected LogHandler() throws RemoteException {
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException {
		// log message
	}
}
