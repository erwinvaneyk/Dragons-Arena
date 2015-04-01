package distributed.systems.network.messagehandlers;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.das.GameState;
import distributed.systems.das.MessageRequest;
import distributed.systems.network.ClientNode;

public class ClientGameActionHandler implements MessageHandler {


	private static final String MESSAGE_TYPE = "DEFAULT";
	private final ClientNode me;
	private int currentId = 0;

	private ConcurrentHashMap<Integer, Message> messageList = new ConcurrentHashMap<>();

	public ClientGameActionHandler(ClientNode me) {
		this.me = me;
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		me.getSocket().logMessage("[" + me.getAddress() + "] received message: (" + message + ")", LogType.DEBUG);
		if(me.getUnit() == null) {
			System.out.println("id:" + (Integer) message.get("id"));
			messageList.put((Integer) message.get("id"), message);
		} else {
			return me.getUnit().onMessageReceived(message);
		}
		return null;
	}

	public Message waitAndGetMessage(int id) {
		// Wait for the reply
		while(!messageList.containsKey(id)) {
			System.out.println("wait:" + id);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return null;
		}
		return messageList.get(id);
	}

	public int nextId() {
		return currentId++;
	}
}
