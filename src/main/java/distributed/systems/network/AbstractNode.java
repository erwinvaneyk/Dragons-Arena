package distributed.systems.network;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.network.logging.InfluxLogger;
import distributed.systems.network.messagehandlers.MessageHandler;
import distributed.systems.network.services.SocketService;
import lombok.Getter;

public abstract class AbstractNode extends UnicastRemoteObject implements IMessageReceivedHandler {
	// MessageHandlers
	private final transient Map<String, MessageHandler> messageHandlers = new HashMap<>();

	// Services
	private final transient ExecutorService services = Executors.newCachedThreadPool();

	private final transient InfluxLogger influxdbLogger = InfluxLogger.getInstance();

	// Address
	@Getter
	private NodeAddress address;

	@Getter
	protected LocalSocket socket;

	@Getter
	protected transient MessageFactory messageFactory;

	protected AbstractNode() throws RemoteException {}


	public void addMessageHandler(MessageHandler messageHandler) {
		this.messageHandlers.put(messageHandler.getMessageType(), messageHandler);
		System.out.println("Added messagehandler for " + messageHandler.getMessageType() + " -> accepted messageTypes: " + getAcceptableMessageTypes());
	}

	public void runService(SocketService socketService) {
		addMessageHandler(socketService);
		services.submit(socketService);
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		System.out.println("received: " + message);
		long start = System.currentTimeMillis();
		message.setReceivedTimestamp();
		Message response = null;
		MessageHandler messageHandler = messageHandlers.get(message.getMessageType());
		if(messageHandler != null) {
			response = messageHandler.onMessageReceived(message);
		} else {
			safeLogMessage("Unable to handle received message: " + message + " (accepted messageTypes: "+ getAcceptableMessageTypes() +"). Ignoring the message!", LogType.WARN);
		}
		influxdbLogger.logMessageDuration(message, getAddress(), System.currentTimeMillis() - start);
		return response;
	}

	public void disconnect() throws RemoteException {
		// Remove old binding
		try {
			UnicastRemoteObject.unexportObject(this, true);
			socket = LocalSocket.connectTo(address);
			socket.unRegister();
		}
		catch (ConnectionException e) {
			System.out.println("Trying to unbind from non-existent registry");
		}
	}

	public void safeLogMessage(String message, LogType type) {
		if(socket != null) {
			try {
				socket.logMessage(message, type);
				return;
			} catch(RuntimeException e) {}
		}
		System.out.println("No socket present: " + message);
	}

	public Collection<String> getAcceptableMessageTypes() {
		return messageHandlers.keySet();
	}

	public abstract NodeType getNodeType();

	//public abstract void setAddress(NodeAddress newAddress);
}
