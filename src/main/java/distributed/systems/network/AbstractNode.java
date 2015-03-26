package distributed.systems.network;

import javax.xml.soap.Node;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.LogMessage;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.network.messagehandlers.MessageHandler;
import distributed.systems.network.services.SocketService;
import lombok.Getter;

public abstract class AbstractNode extends UnicastRemoteObject implements IMessageReceivedHandler {
	// MessageHandlers
	private final Map<String, MessageHandler> messageHandlers = new HashMap<>();

	// Services
	private final ExecutorService services = Executors.newCachedThreadPool();

	// Address
	@Getter
	protected NodeAddress address;

	@Getter
	protected ExtendedSocket socket;

	@Getter
	protected MessageFactory messageFactory;

	protected AbstractNode() throws RemoteException {}


	public void addMessageHandler(MessageHandler messageHandler) {
		this.messageHandlers.put(messageHandler.getMessageType(), messageHandler);
	}

	public void runService(SocketService socketService) {
		services.submit(socketService);
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		message.setReceivedTimestamp();

		MessageHandler messageHandler = messageHandlers.get(message.getMessageType());
		if(messageHandler != null) {
			return messageHandler.onMessageReceived(message);
		} else {
			safeLogMessage("Unable to handle received message: " + message + ". Ignoring the message!", LogType.WARN);
			return null;
		}
	}

	public void disconnect() {
		// Stop services
		services.shutdownNow();

		try {
			// Stop exporting this object
			UnicastRemoteObject.unexportObject(this, true);
			safeLogMessage("Disconnected `" + address + "`.", LogType.INFO);
		}
		catch (NoSuchObjectException e) {
			e.printStackTrace();
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

	//public abstract void setAddress(NodeAddress newAddress);
}
