package distributed.systems.network.services;

import java.rmi.RemoteException;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.network.ClientNode;
import distributed.systems.network.LocalSocket;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeType;
import distributed.systems.network.ServerNode;
import distributed.systems.network.ServerSocket;
import distributed.systems.network.ServerState;

public class NodeBalanceService implements SocketService {

	public static final String MESSAGE_TYPE = "CLIENT_JOIN";

	private static final int MAX_REDIRECTS = 4;

	private final ServerNode me;
	private final ServerSocket serverSocket;

	public NodeBalanceService(ServerNode me) {
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
			serverAddress = ((NodeAddress) response.get("server"));
			socket = LocalSocket.connectTo(serverAddress);
			me.getAddress().setId(myNewAddress.getId());
			me.getAddress().setPhysicalAddress(myNewAddress.getPhysicalAddress());
			socket.logMessage("Added the player `" + me.getAddress() +"` to server `" + serverAddress + "`", LogType.INFO);
		}
		return serverAddress;
	}

	public ServerState getLeastBusyServer() {
		ServerState otherServer = me.getConnectedNodes().stream()
				.filter(node -> node instanceof ServerState)
				.map(node -> (ServerState) node)
				.reduce((a, b) -> a.getClients().size() < b.getClients().size() ? a : b).orElse(me.getServerState());
		return otherServer.getClients().size() < me.getServerState().getClients().size() ? otherServer : me.getServerState();
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		NodeAddress client = message.getOrigin();
		Message response = me.getMessageFactory().createMessage(MESSAGE_TYPE);
		ServerState fromServer = (ServerState) message.get("fromServer");
		if(fromServer != null) me.updateOtherServerState(fromServer);

		if(message.get("forwarded") == null) {
			// Redirect client if necessary
			ServerState redirect = getLeastBusyServer();
			int numbOfRedirects = message.get("redirected") != null ? (Integer) message.get("redirected") : 0;
			if(!redirect.getAddress().equals(me.getAddress()) && numbOfRedirects < MAX_REDIRECTS) {
				serverSocket.logMessage(
						"Redirecting player `" + client + "` from server `" + me.getAddress() + "` to server `" + redirect.getAddress()
								+ "` ",
						LogType.INFO);
				message.put("redirected", numbOfRedirects + 1);
				message.put("fromServer", me.getServerState());
				return serverSocket.sendMessage(message, redirect.getAddress());
			}

			client.setId(me.generateUniqueId(client.getType()));
			client.setPhysicalAddress(me.getAddress().getPhysicalAddress());
			me.updateBindings();
			me.addClient(client);
			// Propagate message
			response.put("address", client);
			response.put("server", me.getNodeState().getAddress());
			serverSocket.broadcast(message.put("forwarded", true).put("server", me.getAddress()), NodeType.SERVER);
			return response;
		} else {
			// Deal with propagated message
			ServerState owner = (ServerState) message.get("fromServer");
			if(owner != null)
				me.updateOtherServerState(owner);
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
