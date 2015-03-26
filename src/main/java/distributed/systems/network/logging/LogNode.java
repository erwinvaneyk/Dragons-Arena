package distributed.systems.network.logging;


import java.rmi.RemoteException;

import distributed.systems.core.LogMessage;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.network.AbstractNode;
import distributed.systems.network.LocalSocket;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.RegistryNode;
import distributed.systems.network.ServerAddress;

/**
 * This node has as one and only task to log everything it receives.
 */
public class LogNode extends AbstractNode {

	private final Logger logger;
	private final RegistryNode ownRegistry;

	public LogNode(int port, NodeAddress node, Logger logger) throws RemoteException {
		// Parent requirements
		ownRegistry = new RegistryNode(port);
		address = new ServerAddress(port, NodeAddress.NodeType.SERVER);
		socket = LocalSocket.connectTo(address);
		socket.register(address);
		messageFactory = new MessageFactory(address);
		// Logger
		this.logger = logger;
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		message.setReceivedTimestamp();
		if(message instanceof LogMessage) {
			logger.log((LogMessage) message);
		} else {
			logger.log(messageFactory.createLogMessage(message, LogType.DEBUG));
		}
		return null;
	}
}
