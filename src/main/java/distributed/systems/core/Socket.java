package distributed.systems.core;

import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeType;

public interface Socket {

	public Message sendMessage(Message message, NodeAddress destination);

	public void logMessage(Message logMessage);

	public void logMessage(String message, LogType type);

	public void broadcast(Message message, NodeType type);

	public void broadcast(Message message);
}
