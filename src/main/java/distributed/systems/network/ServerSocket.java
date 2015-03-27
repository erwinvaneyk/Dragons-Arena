package distributed.systems.network;

import static java.util.stream.Collectors.toList;

import javax.xml.soap.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.istack.internal.NotNull;

import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.core.Socket;
import distributed.systems.das.BattleField;
import lombok.Getter;

/**
 * Deals with connections and messages between servers (communication between multiple RMIRegistries)
 */
public class ServerSocket implements Socket {


	/**
	 * Connections to other servers in the cluster
	 */
	@Getter
	private final ArrayList<ServerAddress> otherNodes;

	@Getter
	private final ServerNode me;


	public ServerSocket(ServerNode me, ArrayList<ServerAddress> otherNodes) {
		this.me = me;
		this.otherNodes = otherNodes;
	}

	public Optional<ServerAddress> getNode(NodeAddress nodeAddress) {
		int index = otherNodes.indexOf(nodeAddress);
		return index > -1 ?  Optional.ofNullable(otherNodes.get(index)) : Optional.empty();

	}

	/**
	 * Currently only for servers
	 */
	public Message sendMessage(Message message, NodeAddress destination) {
		Socket socket = LocalSocket.connectTo(destination);
		return socket.sendMessage(message, destination);
	}

	public void broadcast(Message message) {
		otherNodes.stream().forEach(node -> {
			try {
				sendMessage(message, node);
			}
			catch (RuntimeException e) {
				logMessage("Failed to send message to node `" + node + "`; message: " + message + ", because: " + e,
						LogType.ERROR);
			}
		});
	}

	public void broadcast(Message message, NodeAddress.NodeType type) {
		otherNodes.stream()
				.filter(node -> node.getType().equals(type))
				.forEach(node -> {
					try {
						sendMessage(message, node);
					}
					catch (RuntimeException e) {
						logMessage("Failed to send message to node `" + node + "`; message: " + message + ", because: "
								+ e, LogType.ERROR);
					}
				});
	}

	public void logMessage(Message logMessage) {
		List<NodeAddress> logNodes = otherNodes
				.stream()
				.filter(node -> node.getType().equals(NodeAddress.NodeType.LOGGER))
				.collect(toList());
		logNodes.forEach(logger -> sendMessage(logMessage, logger));
		// If there are no loggers, just output it to the screen.
		if(logNodes.isEmpty()) {
			System.out.println("No logger present: " + logMessage);
		}
	}

	public void logMessage(String message, LogType type) {
		logMessage(me.getMessageFactory().createLogMessage(message, type));

	}
}
