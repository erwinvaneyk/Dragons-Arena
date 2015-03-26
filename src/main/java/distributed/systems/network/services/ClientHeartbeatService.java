package distributed.systems.network.services;

import java.util.List;

import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.network.AbstractNode;
import distributed.systems.network.ServerAddress;

public class ClientHeartbeatService extends HeartbeatService {

	public ClientHeartbeatService(AbstractNode me, Socket socket, List<ServerAddress> heartbeatNodes) {
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
		ExtendedSocket localsocket = (ExtendedSocket) socket;
		localsocket.sendMessage(message, localsocket.getRegistryAddress());

	}
}
