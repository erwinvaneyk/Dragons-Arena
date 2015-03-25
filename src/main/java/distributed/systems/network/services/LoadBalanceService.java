package distributed.systems.network.services;

import java.rmi.RemoteException;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.network.LocalSocket;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerAddress;
import distributed.systems.network.ServerSocket;

public class LoadBalanceService implements SocketService {

	private static final int MAX_REDIRECTS = 4;

	private final ServerSocket serverSocket;
	private final ServerAddress address;

	public LoadBalanceService(ServerSocket serverSocket, ServerAddress address) {
		this.serverSocket = serverSocket;
		this.address = address;
	}

	public void rebalance() {
		// TODO rebalance session
	}

	public NodeAddress getLeastBusyServer() {
		ServerAddress otherServer = serverSocket.getOtherNodes().stream()
				.filter(NodeAddress::isServer)
				.reduce((a, b) -> a.getClients().size() < b.getClients().size() ? a : b)
				.orElseThrow(() -> new RuntimeException("No server could be found be the load balancer"));
		return otherServer.getClients().size() < address.getClients().size() ? otherServer : address;
	}



	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		NodeAddress client = message.getOrigin();
		Message response = serverSocket.getMessageFactory().createMessage(Message.Type.JOIN_SERVER);

		if(message.get("forwarded") == null) {
			// Redirect client if necessary
			NodeAddress redirect = getLeastBusyServer();
			int numbOfRedirects = message.get("redirected") != null ? (Integer) message.get("redirected") : 0;
			if(!redirect.equals(address) && numbOfRedirects < MAX_REDIRECTS) {
				serverSocket.logMessage(
						"Redirecting player `" + client + "` from server `" + address + "` to server `" + redirect
								+ "` ",
						LogType.INFO);
				message.put("redirected", numbOfRedirects + 1);
				return serverSocket.sendMessage(message, redirect);
			}

			client.setId(serverSocket.generateUniqueId(client));
			client.setPhysicalAddress(address.getPhysicalAddress());
			response.put("address", client);
			response.put("server", address);
			serverSocket.getOtherNodes().add(new ServerAddress(client));
			address.getClients().add(client);
			// Propagate message
			serverSocket.broadcast(message.put("forwarded", true).put("server", address), NodeAddress.NodeType.SERVER);
			return response;
		} else {
			ServerAddress owner = (ServerAddress) message.get("server");
			serverSocket.getOtherNodes().add(new ServerAddress(client));
			serverSocket.getNode(owner).get().getClients().add(client);
			return null;
		}
	}
}
