package distributed.systems.network.services;

import java.util.List;

import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.network.AbstractNode;
import distributed.systems.network.ServerAddress;

public class ClientHeartbeatService extends HeartbeatService {

	private final ExtendedSocket localsocket;

	public ClientHeartbeatService(AbstractNode me, ExtendedSocket socket) {
		super(me, socket);
		this.localsocket = socket;
	}

	// TODO: do some cleanup, moving the clients of a disconnected server to other servers
	protected void removeNode(ServerAddress address) {
		nodes.remove(address);
		watchNodes.remove(address);
		socket.logMessage("Node `" + address.getName() + "` TIMED OUT, because it has not been sending any heartbeats!",
				LogType.WARN);

	}

	public void doHeartbeat() {
		Message message = me.getMessageFactory().createMessage(HeartbeatService.MESSAGE_TYPE);
		localsocket.sendMessage(message, localsocket.getRegistryAddress());
	}
}
