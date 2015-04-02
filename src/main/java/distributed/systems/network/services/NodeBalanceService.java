package distributed.systems.network.services;

import static java.util.stream.Collectors.toList;

import java.rmi.RemoteException;
import java.util.List;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.network.ClientNode;
import distributed.systems.network.ConnectionException;
import distributed.systems.network.LocalSocket;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeType;
import distributed.systems.network.ServerNode;
import distributed.systems.network.ServerState;

public class NodeBalanceService implements SocketService {

	public static final String CLIENT_JOIN = "CLIENT_JOIN";

	public static final long BALANCE_INTERVAL = 10000;

	private static final int MAX_REDIRECTS = 4;
	public static final float INBALANCE_THRESHOLD = 0.7F; // the lowest loaded server has 0.7 load of the most loaded server

	private final ServerNode me;

	public NodeBalanceService(ServerNode me) {
		this.me = me;
	}

	public boolean moveClientToServer(NodeAddress client, NodeAddress newServer) {
		NodeAddress oldClient = new NodeAddress(client);
		Message moveMessage = me.getMessageFactory().createMessage(CLIENT_JOIN)
				.put("action", "move")
				.put("newServer", newServer)
				.put("client", client);
		try {
			Message response = me.getServerSocket().sendMessage(moveMessage, newServer);
			if(response != null && response.get("success") != null) {
				//me.removeClient(oldClient);
				return true;
			} else {
				throw new ConnectionException("Response from other server was not success");
			}
		}
		catch (ConnectionException e) {
			me.safeLogMessage("Failed to move client to other server: " + client + " -> " + newServer, LogType.ERROR);
		}
		return false;
	}

	public static NodeAddress joinServer(ClientNode me, NodeAddress serverAddress) throws ConnectionException {
		// Connect to cluster (initially before being rebalanced)
		Socket socket = LocalSocket.connectTo(serverAddress);
		// Acknowledge
		System.out.println(socket);
		Message response = socket.sendMessage(me.getMessageFactory().createMessage(CLIENT_JOIN), serverAddress);
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
		if(message.get("action") != null) {
			return onClientMove(message);
		} else {
			return onClientJoin(message);
		}
	}

	// Received by the server receiving the client
	private Message onClientMove(Message message) {
		NodeAddress client = (NodeAddress) message.get("client");
		Message toClient = me.getMessageFactory().createMessage(CLIENT_JOIN)
				.put("action", "move")
				.put("newServer", me.getAddress());
		try {
			Message response = me.getServerSocket().sendMessage(toClient, client);
			if(response != null && response.get("success") != null) {
				me.addClient(client);
			} else {
				throw new ConnectionException("Failed to move client " + client + " to this server: " + response);
			}
			return response;
		}
		catch (ConnectionException e) {
			me.safeLogMessage("Failed to move client " + client + " to this server " + me.getAddress(), LogType.ERROR);
			return null;
		}
	}

	private Message onClientJoin(Message message) {
		NodeAddress client = message.getOrigin();
		Message response = me.getMessageFactory().createMessage(CLIENT_JOIN);
		ServerState fromServer = (ServerState) message.get("fromServer");
		if(fromServer != null) me.updateOtherServerState(fromServer);

		if(message.get("forwarded") == null) {
			// Redirect client if necessary
			if(message.get("noredirect") == null) {
				ServerState redirect = getLeastBusyServer();
				int numbOfRedirects = message.get("redirected") != null ? (Integer) message.get("redirected") : 0;
				if (!redirect.getAddress().equals(me.getAddress()) && numbOfRedirects < MAX_REDIRECTS) {
					me.getServerSocket().logMessage(
							"Redirecting player `" + client + "` from server `" + me.getAddress() + "` to server `"
									+ redirect.getAddress()
									+ "` ",
							LogType.INFO);
					message.put("redirected", numbOfRedirects + 1);
					message.put("fromServer", me.getServerState());
					try {
						return me.getServerSocket().sendMessage(message, redirect.getAddress());
					}
					catch (ConnectionException e) {
						me.safeLogMessage("Failed to redirect to " + redirect.getAddress() + ", sticking to " + me.getAddress(), LogType.WARN);
					}
				}
			}
			client.setId(me.generateUniqueId(client.getType()));
			client.setPhysicalAddress(me.getAddress().getPhysicalAddress());
			try {
				me.updateBindings();
				me.addClient(client);
				// Propagate message
				response.put("address", client);
				response.put("server", me.getNodeState().getAddress());
				me.getServerSocket().broadcast(message.put("forwarded", true).put("server", me.getAddress()),
						NodeType.SERVER);
				return response;
			}
			catch (ConnectionException e) {
				me.safeLogMessage("While adding a client, failed to update bindings of " + me.getAddress(), LogType.WARN);
			}
		} else {
			// Deal with propagated message
			ServerState owner = (ServerState) message.get("fromServer");
			if(owner != null)
				me.updateOtherServerState(owner);
		}
		return null;
	}

	@Override
	public String getMessageType() {
		return CLIENT_JOIN;
	}

	@Override
	public void run() {
		try {
			while(!Thread.currentThread().isInterrupted()) {
				balanceServers();
				Thread.sleep(BALANCE_INTERVAL);
			}
		} catch (InterruptedException e) {
			me.safeLogMessage("Heartbeat service has been stopped", LogType.INFO);
		}
	}

	private void balanceServers() {
		List<ServerState> servers = me.getConnectedNodes()
				.stream()
				.filter(c -> c.getAddress().isServer())
				.map(c -> (ServerState) c)
				.collect(toList());

		if(servers.size() < 1) {
			me.safeLogMessage("No need for balancing in a single server cluster!", LogType.DEBUG);
			return;
		}

		ServerState leastLoaded = servers.stream().reduce((a,b) -> b.getClients().size() > a.getClients().size() ? a : b).get();
		float inbalance = (float) me.getServerState().getClients().size() != 0 ? (float) leastLoaded.getClients().size() / (float) me.getServerState().getClients().size() : 1;
		if(inbalance < INBALANCE_THRESHOLD && me.getServerState().getClients().size() > 1) {
			me.safeLogMessage("Balancing the cluster, because inbalance was " + inbalance, LogType.INFO);
			NodeAddress clientToMove = me.getServerState().getClients().iterator().next();
			me.moveClient(clientToMove, leastLoaded.getAddress());
			balanceServers();
		} else {
			me.safeLogMessage("Cluster is in balance; the inbalance was " + inbalance, LogType.DEBUG);
		}
	}
}
