package distributed.systems.network.services;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.network.AbstractNode;
import distributed.systems.network.NodeAddress;

public abstract class HeartbeatService implements SocketService {

	public static final String MESSAGE_TYPE = "HEARTBEAT";

	protected static final int TIMEOUT_DURATION = 15000;
	protected static final int CHECK_INTERVAL = 5000;

	protected final Map<NodeAddress, Integer> watchNodes = new ConcurrentHashMap<>();
	protected final Socket socket;
	protected final AbstractNode me;

	public HeartbeatService(AbstractNode me, Socket socket) {
		this.socket = socket;
		this.me = me;
	}

	public HeartbeatService expectHeartbeatFrom(NodeAddress node) {
		watchNodes.put(node, TIMEOUT_DURATION / CHECK_INTERVAL);
		return this;
	}

	public HeartbeatService expectHeartbeatFrom(Collection<NodeAddress> nodes) {
		nodes.stream().forEach(this::expectHeartbeatFrom);
		return this;
	}


	public void run() {
		me.safeLogMessage("Starting heartbeat process...", LogType.DEBUG);
		try {
			while(!Thread.currentThread().isInterrupted()) {
				doHeartbeat();
				checkHeartbeats();
				Thread.sleep(CHECK_INTERVAL);
			}
		} catch (InterruptedException e) {
			socket.logMessage("Heartbeat service has been stopped", LogType.INFO);
		}
	}

	private void checkHeartbeats() {
		// Count down nodes
		watchNodes.entrySet().stream().forEach(node -> {
			if(node.getValue() < 0) {
				removeNode(node.getKey());
			} else {
				watchNodes.put(node.getKey(), node.getValue() - 1);
			}
		});
	}

	// TODO: do some cleanup, moving the clients of a disconnected server to other servers
	protected abstract void removeNode(NodeAddress address);

	public abstract void doHeartbeat();

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		NodeAddress origin = message.getOrigin();
		watchNodes.entrySet().stream()
				.filter(node -> node.getKey().equals(origin))
				.findAny()
				.ifPresent(node -> {
					node.setValue(TIMEOUT_DURATION / CHECK_INTERVAL);
					//socket.logMessage("Received a heartbeat from node `" + node.getKey().getName() + ".", LogType.DEBUG);
				});
		return null;
	}
	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}

	public void remove(NodeAddress client) {
		watchNodes.remove(client);
	}
}
