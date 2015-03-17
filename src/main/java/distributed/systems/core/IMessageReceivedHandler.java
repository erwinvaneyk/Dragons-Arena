package distributed.systems.core;

public interface IMessageReceivedHandler {

	void onMessageReceived(Message message);
}
