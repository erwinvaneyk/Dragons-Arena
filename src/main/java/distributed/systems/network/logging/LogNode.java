package distributed.systems.network.logging;


import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

import distributed.systems.core.LogMessage;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.network.AbstractServerNode;
import distributed.systems.network.LocalSocket;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.RegistryNode;
import distributed.systems.network.ServerAddress;
import distributed.systems.network.ServerSocket;
import distributed.systems.network.messagehandlers.ServerJoinHandler;
import lombok.Getter;
import lombok.Setter;

/**
 * This node has as one and only task to log everything it receives.
 */
public class LogNode extends AbstractServerNode {

	@Setter @Getter
	private long flushThreshold = 10000;

	private final Logger logger;
	private final PriorityQueue<Message> orderingQueue;

	public LogNode(int port, Logger logger) throws RemoteException {
		// Parent requirements
		ownRegistry = new RegistryNode(port);
		address = new ServerAddress(port, NodeAddress.NodeType.LOGGER);
		this.logger = logger;
		this.messageFactory = new MessageFactory(address);

		orderingQueue = new PriorityQueue<>(10, new Comparator<Message>() {
			@Override
			public int compare(Message o1, Message o2) {
				return (int) (o2.getTimestamp().getTime() - o1.getTimestamp().getTime());
			}
		});

		socket = LocalSocket.connectTo(address);
		socket.register(address);
		socket.addMessageReceivedHandler(this);
		messageFactory = new MessageFactory(address);
		socket.logMessage("Logger " + address + " is configured and ready for some heavy logging!", LogType.DEBUG);
		serverSocket = new ServerSocket(this, otherNodes);
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		message.setReceivedTimestamp();
		orderingQueue.add(message);
		flushMessagesOlderThan(System.currentTimeMillis() - flushThreshold);
		return null;
	}

	public void flushMessagesOlderThan(long timestamp) {
		Message nextElement;
		do {
			nextElement = orderingQueue.poll();
			log(orderingQueue.poll());
		} while(nextElement != null && nextElement.getTimestamp().getTime() < timestamp);
		if(nextElement != null) {
			orderingQueue.add(nextElement);
		}
	}

	private void log(Message message) {
		if(message != null) {
			if (message instanceof LogMessage) {
				logger.log((LogMessage) message);
			}
			else {
				logger.log(messageFactory.createLogMessage(message, LogType.DEBUG));
			}
		}
	}
}
