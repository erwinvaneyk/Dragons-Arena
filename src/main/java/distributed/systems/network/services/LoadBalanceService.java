package distributed.systems.network.services;

import java.rmi.RemoteException;

import distributed.systems.core.Message;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerSocket;

public class LoadBalanceService implements SocketService {

	private final ServerSocket serverSocket;
	private final NodeAddress address;

	public LoadBalanceService(ServerSocket serverSocket, NodeAddress address) {
		this.serverSocket = serverSocket;
		this.address = address;
	}



	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		NodeAddress client = message.getOrigin();
		Message response = serverSocket.getMessageFactory().createMessage(Message.Type.JOIN_SERVER);
		// TODO check if load balanced, if not redirect

		// load okay, generate id
		if(message.get("forwarded") == null) {
			client = serverSocket.generateUniqueId(client);
			client.setPhysicalAddress(address.getPhysicalAddress());
			response.put("address", client);
			serverSocket.getOtherNodes().add(client);
			serverSocket.getMyClients().add(client);
			// Propagate message
			serverSocket.broadcast(message.put("forwarded", true));
			return response;
		} else {
			serverSocket.getOtherNodes().add(client);
			return null;
		}
	}
}
