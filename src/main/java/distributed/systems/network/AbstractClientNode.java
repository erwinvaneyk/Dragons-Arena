package distributed.systems.network;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractClientNode extends AbstractNode implements ClientNode {

	protected AbstractClientNode() throws RemoteException {

	}

	@Getter
	@Setter
	private transient Set<NodeAddress> backupServers = new HashSet<>();

	@Override
	public Message sendMessageToServer(Message message) {
		try {
			return socket.sendMessage(message, this.getServerAddress());
		} catch (ClusterException e) {
			// Lets try again
			safeLogMessage("Failed to send message to " + this.getServerAddress() + ". Lets try again!", LogType.WARN);
			try {
				Thread.sleep(50);
				return socket.sendMessage(message, this.getServerAddress());
			}
			catch (InterruptedException | ClusterException e1) {
				safeLogMessage("Failed to send message again! Lets try another server", LogType.WARN);
				joinBackupServer();
				return sendMessageToServer(message);
			}
		}
	}

	public void joinBackupServer() {
		if(!getBackupServers().isEmpty()) {
			NodeAddress backupServer = getBackupServers().iterator().next();
			getBackupServers().remove(backupServer);
			try {
				joinServer(backupServer);
			}
			catch (ConnectionException e2) {
				safeLogMessage("Failed to connect to backup server " + backupServer + "! Lets try another server", LogType.WARN);
			}
		} else {
			throw new RuntimeException("Failed to reconnect to the cluster, it is probably offline.");
		}
	}
}
