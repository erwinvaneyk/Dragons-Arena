package distributed.systems.core;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import distributed.systems.das.BattleField;
import lombok.ToString;

/**
 * Entry-point for incoming messages for the battlefield
 */
@ToString
public class BattlefieldProxy extends UnicastRemoteObject implements IMessageProxyHandler {

	private final BattleField subject;

	public BattlefieldProxy(BattleField subject) throws RemoteException {
		this.subject = subject;
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException {
		message.setReceivedTimestamp();
		// TODO: put message into queue
		this.subject.onMessageReceived(message);
	}
}
