package distributed.systems.network.messagehandlers;

import java.util.ArrayList;

import com.sun.istack.internal.NotNull;
import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.core.Socket;
import distributed.systems.network.AbstractServerNode;
import distributed.systems.network.LocalSocket;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerAddress;
import distributed.systems.network.ServerNode;

public class ServerConnectHandler implements MessageHandler {

	public static final String MESSAGE_TYPE = "HANDSHAKE";

	private final AbstractServerNode me;
	private final MessageFactory messageFactory;

	public ServerConnectHandler(AbstractServerNode node) {
		this.me = node;
		this.messageFactory = me.getMessageFactory();
	}

	public Message onMessageReceived(Message message) {
		Message response = null;
		ServerAddress newServer = (ServerAddress) message.get("address");
		// TODO: check if address is unique
		if(message.get("forwarded") == null) {
			ArrayList<ServerAddress> servers = new ArrayList<>(me.getOtherNodes());
			servers.add(me.getServerAddress());
			response = messageFactory.createMessage(message.getMessageType())
					.put("servers", servers);

			// Determine id for new server if necessary
			if(newServer.getId() < 0) { // Indicates that server has no id yet
				String oldServerId = newServer.toString();
				newServer.setId(me.generateUniqueId(newServer.getType()));
				response.put("address", newServer);
				message.put("address", newServer);
				me.getServerSocket().logMessage(
						"Assigned ID to new server, renamed `" + oldServerId + "` to `" + newServer + "`, otherNodes: " + me
								.getOtherNodes() + ".", LogType.DEBUG);
			}
			// Propagate
			message.put("forwarded", true);
			me.getServerSocket().broadcast(message);
		}
		// Add new server to list
		me.addServer(new ServerAddress(newServer));
		return response;
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}
}
