package distributed.systems.network.logging;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.LogMessage;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.core.Socket;
import distributed.systems.core.SynchronizedSocket;
import distributed.systems.network.LocalSocket;
import distributed.systems.network.NodeAddress;

/**
 * This node has as one and only task to log everything it receives.
 */
public class LogNode extends UnicastRemoteObject implements IMessageReceivedHandler {

	private final Logger logger;
	private final ExtendedSocket socket;
	private final NodeAddress address;
	private final MessageFactory messageFactory;

	public static void main(String[] args) throws RemoteException {
		new LogNode(Logger.getDefault());
	}

	public LogNode(Logger logger) throws RemoteException {
		// Connect to cluster
		socket = LocalSocket.connectToDefault();
		address = socket.determineAddress(NodeAddress.NodeType.LOGGER);
		socket.register(address);
		socket.addMessageReceivedHandler(this);
		this.logger = logger;
		this.messageFactory = new MessageFactory(address);
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		message.setReceivedTimestamp();
		if(message instanceof LogMessage) {
			logger.log((LogMessage) message);
		} else {
			logger.log(messageFactory.createLogMessage(message));
		}
		return null;
	}
}
