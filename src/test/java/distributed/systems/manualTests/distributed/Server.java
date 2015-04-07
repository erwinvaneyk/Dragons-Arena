package distributed.systems.manualTests.distributed;

import java.rmi.RemoteException;

import distributed.systems.network.ConnectionException;
import distributed.systems.network.ServerNode;

public class Server {

	public static void main(String[] args) throws RemoteException, ConnectionException {
		ServerNode server1 = new ServerNode(2355);
		server1.startCluster();
		System.out.println(server1.getAddress());
		server1.launchViewer();
	}
}
