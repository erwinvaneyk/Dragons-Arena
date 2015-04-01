package distributed.systems.example.distributed;

import java.rmi.RemoteException;

import distributed.systems.network.ServerNode;

public class Server {

	public static void main(String[] args) throws RemoteException {
		ServerNode server1 = new ServerNode(2345);
		server1.startCluster();
		System.out.println(server1.getAddress());
		server1.launchViewer();
	}
}
