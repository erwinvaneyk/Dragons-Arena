package distributed.systems.core;

import java.util.List;

import distributed.systems.network.NodeAddress;

public interface Socket {

	public Message sendMessage(Message message, NodeAddress destination);

	public void logMessage(Message logMessage);

	public void logMessage(String message, LogType type);

	public void broadcast(Message message, NodeAddress.NodeType type);

	public void broadcast(Message message);
}
