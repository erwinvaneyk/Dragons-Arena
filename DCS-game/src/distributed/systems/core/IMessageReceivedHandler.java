package distributed.systems.core;

import java.rmi.Remote;

public interface IMessageReceivedHandler extends Remote {

	void onMessageReceived(Message message);
}
