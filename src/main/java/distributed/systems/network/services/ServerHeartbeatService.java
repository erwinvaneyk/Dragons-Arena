package distributed.systems.network.services;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.core.Socket;
import distributed.systems.network.BasicNode;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerAddress;
import distributed.systems.network.ServerSocket;

public class ServerHeartbeatService extends HeartbeatService {

	public ServerHeartbeatService(BasicNode me, Socket socket, List<ServerAddress> heartbeatNodes) {
		super(me, socket, heartbeatNodes);
	}

	// TODO: do some cleanup, moving the clients of a disconnected server to other servers
	protected void removeNode(ServerAddress address) {
		nodes.remove(address);
		heartbeatNodes.remove(address);
		socket.logMessage("Node `" + address.getName() + "` TIMED OUT, because it has not been sending any heartbeats!",
				LogType.WARN);

	}

	public void doHeartbeat() {
		Message message = me.getMessageFactory().createMessage(HeartbeatService.MESSAGE_TYPE);
		ServerSocket serversocket = (ServerSocket) socket;
		// Fast hack to get my clients
		socket.broadcast(message, NodeAddress.NodeType.SERVER);
		serversocket.getMe().getServerAddress().getClients().stream().forEach(node -> {
			try {
				socket.sendMessage(message, node);
			}
			catch (RuntimeException e) {
				socket.logMessage(
						"Failed to send message to node `" + node + "`; message: " + message + ", because: "
								+ e, LogType.ERROR);
			}
		});

	}
}
