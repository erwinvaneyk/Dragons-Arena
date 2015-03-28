package distributed.systems.example;

import java.rmi.RemoteException;

import distributed.systems.core.Socket;
import distributed.systems.network.LocalSocket;
import distributed.systems.network.ServerNode;

public class TestSetupSimple {

	public static void main(String[] args) throws RemoteException {
		ServerNode server1 = new ServerNode(2345);
		server1.startCluster();
		ServerNode server2 = new ServerNode(2346);
		server2.connect(server1.getServerAddress());
		System.out.println("ALL GOOD!!!!");
	}
}
