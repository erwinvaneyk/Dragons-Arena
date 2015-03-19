package distributed.systems.core;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import distributed.systems.das.units.Unit;
import lombok.ToString;

/**
 * Entry-point for incoming messages
 */
@ToString
public class UnitProxy extends UnicastRemoteObject implements IMessageProxyHandler {

	private final Unit subject;

	public UnitProxy(Unit subject) throws RemoteException {
		this.subject = subject;
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException {
		message.setReceivedTimestamp();
		// TODO: if server/battlefield put message into queue
		this.subject.onMessageReceived(message);
	}
}
