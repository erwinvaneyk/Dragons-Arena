package distributed.systems.network;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.das.MessageRequest;
import lombok.Getter;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Deals with connections and messages between servers (communication between multiple RMIRegistries)
 */
public class ServerSocket implements Socket {

	@Getter
	private final AbstractServerNode me;


	public ServerSocket(AbstractServerNode me) {
		this.me = me;
	}

	/**
	 * Currently only for servers
	 */
	public Message sendMessage(Message message, NodeAddress destination) throws ConnectionException {
		Socket socket = LocalSocket.connectTo(destination);
		return socket.sendMessage(message, destination);
	}

	public void broadcast(Message message) {
		me.getConnectedNodes().stream().forEach(node -> {
			try {
				sendMessage(message, node.getAddress());
			}
			catch (RuntimeException | ConnectionException e) {
				//e.printStackTrace();
				logMessage("Failed to send message to node `" + node + "`; message: " + message + ", because: " + e,
						LogType.ERROR);
			}
		});
	}

	public void broadcast(Message message, NodeType type) {
        me.getConnectedNodes().stream().map(NodeState::getAddress)
                .filter(node -> node.getType().equals(type))
                .forEach(node -> {
                    try {
                        sendMessage(message, node);
                    } catch (RuntimeException | ConnectionException e) {
                        logMessage("Failed to send message to node `" + node + "`; message: " + message + ", because: "
                                + e, LogType.ERROR);
                    }
                });
	}

	public void logMessage(Message logMessage) {
		if(logMessage == null) return;
		List<NodeState> logNodes = me.getConnectedNodes()
				.stream()
				.filter(node -> node.getAddress().getType().equals(NodeType.LOGGER))
				.collect(toList());
		logNodes.forEach(logger -> {
			try {
				sendMessage(logMessage, logger.getAddress());
			} catch (RuntimeException | ConnectionException e) {
				System.out.println(logMessage);
				e.printStackTrace();
				System.out.println("Error occured while trying to log! We do not really care about logs anyway, moving on.. Reason: " + e);
			}
		});
		// If there are no loggers, just output it to the screen.
		if(me.getNodeType() == NodeType.LOGGER) {
			try {
				sendMessage(logMessage, me.getAddress());
			} catch (ConnectionException e) {
				System.out.println("No connection to own registry: " + logMessage);
			}
		} else if(logNodes.isEmpty()) {
			System.out.println("No logger found: " + logMessage);
		}
	}

	public void logMessage(String message, LogType type) {
		logMessage(me.getMessageFactory().createLogMessage(message, type));

	}
}
