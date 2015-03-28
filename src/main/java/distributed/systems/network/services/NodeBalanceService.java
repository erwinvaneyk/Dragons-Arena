package distributed.systems.network.services;

import java.rmi.RemoteException;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.network.AbstractServerNode;
import distributed.systems.network.ClientNode;
import distributed.systems.network.LocalSocket;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerAddress;
import distributed.systems.network.ServerNode;
import distributed.systems.network.ServerSocket;

public class NodeBalanceService implements SocketService {

	public static final String MESSAGE_TYPE = "CLIENT_JOIN";

	private static final int MAX_REDIRECTS = 4;

	private final AbstractServerNode me;
	private final ServerSocket serverSocket;

	public NodeBalanceService(AbstractServerNode me) {
		this.me = me;
		this.serverSocket = me.getServerSocket();
	}

	public void rebalance() {
		// TODO rebalance session
	}

	public static NodeAddress joinServer(ClientNode me, NodeAddress serverAddress) {
		// Connect to cluster (initially before being rebalanced)
		Socket socket = LocalSocket.connectTo(serverAddress);
		// Acknowledge
		Message response = socket.sendMessage(me.getMessageFactory().createMessage(MESSAGE_TYPE), serverAddress);
		if(response.get("redirect") != null) {
			NodeAddress redirectedAddress = (NodeAddress) response.get("redirect");
			// Attempt to join the other server
			socket.logMessage("Being redirected from `" + serverAddress + "` to `" + redirectedAddress + "`", LogType.INFO);
			return joinServer(me, redirectedAddress);
		} else {
			// Join this server
			NodeAddress myNewAddress = (NodeAddress) response.get("address");
			serverAddress = (ServerAddress) response.get("server");
			socket = LocalSocket.connectTo(serverAddress);
			me.getAddress().setId(myNewAddress.getId());
			me.getAddress().setPhysicalAddress(myNewAddress.getPhysicalAddress());
			socket.logMessage("Added the player `" + me.getAddress() +"` to server `" + serverAddress + "`", LogType.INFO);
		}
		return serverAddress;
	}

	public NodeAddress getLeastBusyServer() {
		ServerAddress otherServer = me.getOtherNodes().stream()
				.filter(NodeAddress::isServer)
				.reduce((a, b) -> a.getClients().size() < b.getClients().size() ? a : b).orElse(me.getServerAddress());
		return otherServer.getClients().size() < me.getServerAddress().getClients().size() ? otherServer : me.getAddress();
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		NodeAddress client = message.getOrigin();
		Message response = me.getMessageFactory().createMessage(MESSAGE_TYPE);

		if(message.get("forwarded") == null) {
			// Redirect client if necessary
			NodeAddress redirect = getLeastBusyServer();
			int numbOfRedirects = message.get("redirected") != null ? (Integer) message.get("redirected") : 0;
			if(!redirect.equals(me.getAddress()) && numbOfRedirects < MAX_REDIRECTS) {
				serverSocket.logMessage(
						"Redirecting player `" + client + "` from server `" + me.getAddress() + "` to server `" + redirect
								+ "` ",
						LogType.INFO);
				message.put("redirected", numbOfRedirects + 1);
				return serverSocket.sendMessage(message, redirect);
			}

			client.setId(me.generateUniqueId(client));
			client.setPhysicalAddress(me.getAddress().getPhysicalAddress());
			me.updateBindings();
			response.put("address", client);
			response.put("server", me.getAddress());
			serverSocket.getOtherNodes().add(new ServerAddress(client));
			me.getServerAddress().getClients().add(client);
			// Propagate message
			serverSocket.broadcast(message.put("forwarded", true).put("server", me.getAddress()), NodeAddress.NodeType.SERVER);
			return response;
		} else {
			ServerAddress owner = (ServerAddress) message.get("server");
			serverSocket.getOtherNodes().add(new ServerAddress(client));
			serverSocket.getNode(owner).get().getClients().add(client);
			return null;
		}
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public void run() {}
}
