package distributed.systems.core;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MessageProxy extends UnicastRemoteObject implements IMessageProxyHandler, Serializable {

	private final IMessageReceivedHandler subject;

	public MessageProxy(IMessageReceivedHandler subject) throws RemoteException {
		this.subject = subject;
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException {
		this.subject.onMessageReceived(message);
	}
}
