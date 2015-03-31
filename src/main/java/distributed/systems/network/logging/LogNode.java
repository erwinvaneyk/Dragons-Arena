package distributed.systems.network.logging;


import static java.util.stream.Collectors.toList;

import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.PriorityQueue;

import distributed.systems.core.LogMessage;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.network.AbstractServerNode;
import distributed.systems.network.ClusterException;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeState;
import distributed.systems.network.NodeType;
import distributed.systems.network.ServerState;
import distributed.systems.network.messagehandlers.ServerConnectHandler;
import distributed.systems.network.services.ServerHeartbeatService;
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
	private final ServerHeartbeatService heartbeatService;

	public LogNode(int port, Logger logger) throws RemoteException {
		super(port);
		this.logger = logger;

		orderingQueue = new PriorityQueue<>(10, new Comparator<Message>() {
			@Override
			public int compare(Message o1, Message o2) {
				return (int) (o2.getTimestamp().getTime() - o1.getTimestamp().getTime());
			}
		});
		heartbeatService = new ServerHeartbeatService(this, serverSocket);
		addMessageHandler(heartbeatService);
		addMessageHandler(new ServerConnectHandler(this));
		log(messageFactory.createLogMessage("Logger " + getAddress() + " is ready to connect and do some heavy logging!",
				LogType.INFO));
	}

	public void addServer(NodeState server) {
		super.addServer(server);
		heartbeatService.expectHeartbeatFrom(server.getAddress());
	}

	public void connect(NodeAddress server) {
		try {
			super.connect(server);
			runService(heartbeatService);
			heartbeatService.expectHeartbeatFrom(this.getConnectedNodes().keySet().stream().map(NodeState::getAddress).collect(toList()));
		} catch (ClusterException e) {
			log(messageFactory.createLogMessage("Could not connect logger to cluster at " + server, LogType.ERROR));
		}
		log(messageFactory.createLogMessage("Connected server to the cluster of " + ownRegistry, LogType.INFO));
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		if(!message.getMessageType().equals(LogMessage.MESSAGE_TYPE)) {
			return super.onMessageReceived(message);
		} else {
			message.setReceivedTimestamp();
			orderingQueue.add(message);
			flushMessagesOlderThan(System.currentTimeMillis() - flushThreshold);
		}
		return null;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.LOGGER;
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
