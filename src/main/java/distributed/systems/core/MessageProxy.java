package distributed.systems.core;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import lombok.ToString;

/**
 * Entry-point for incoming messages
 */
@ToString
public class MessageProxy extends UnicastRemoteObject implements IMessageProxyHandler, Serializable {

	private final IMessageReceivedHandler subject;

	public MessageProxy(IMessageReceivedHandler subject) throws RemoteException {
		this.subject = subject;
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException {
		// TODO: if server/battlefield put message into queue
		this.subject.onMessageReceived(message);
	}
}
