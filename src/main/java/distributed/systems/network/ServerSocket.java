package distributed.systems.network;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.das.MessageRequest;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

import javax.xml.soap.Node;

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
	public Message sendMessage(Message message, NodeAddress destination) {
        if (message.get("request")!=null && message.get("request").equals(MessageRequest.spawnUnit)){
            System.out.println("IN Server Socket "+this.me.getAddress()+" send a spawn message to "+destination.getName());
        }
		Socket socket = LocalSocket.connectTo(destination);
		return socket.sendMessage(message, destination);
	}

	public void broadcast(Message message) {
		me.getConnectedNodes().keySet().stream().forEach(node -> {
			try {
				sendMessage(message, node.getAddress());
			}
			catch (RuntimeException e) {
				e.printStackTrace();
				logMessage("Failed to send message to node `" + node + "`; message: " + message + ", because: " + e,
						LogType.ERROR);
			}
		});
	}

	public void broadcast(Message message, NodeType type) {
        if (message.get("request")!=null && message.get("request").equals(MessageRequest.spawnUnit)) {
            System.out.println("In the ServerSocket " + this.getMe().getAddress().getName() + "send a broadcast message");
        }
		me.getConnectedNodes().keySet().stream().map(NodeState::getAddress)
				.filter(node -> node.getType().equals(type))
				.forEach(node -> {
					try {
						sendMessage(message, node);
					}
					catch (RuntimeException e) {
						e.printStackTrace();
						logMessage("Failed to send message to node `" + node + "`; message: " + message + ", because: "
								+ e, LogType.ERROR);
					}
				});
	}

	public void logMessage(Message logMessage) {
		List<NodeState> logNodes = me.getConnectedNodes().keySet()
				.stream()
				.filter(node -> node.getNodeType().equals(NodeType.LOGGER))
				.collect(toList());
		logNodes.forEach(logger -> {
			try {
				sendMessage(logMessage, logger.getAddress());
			} catch (RuntimeException e) {
				System.out.println("Error occured while trying to log! We do not really care about logs anyway, moving on.. Reason: " + e);
			}
		});
		// If there are no loggers, just output it to the screen.
		if(me.getNodeType() == NodeType.LOGGER) {
			sendMessage(logMessage, me.getAddress());
		} else if(logNodes.isEmpty()) {
			System.out.println("No logger found: " + logMessage);
		}
	}

	public void logMessage(String message, LogType type) {
		logMessage(me.getMessageFactory().createLogMessage(message, type));

	}
}
