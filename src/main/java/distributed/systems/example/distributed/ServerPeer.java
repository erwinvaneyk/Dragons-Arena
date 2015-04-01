package distributed.systems.example.distributed;

import java.rmi.RemoteException;

import distributed.systems.launchers.*;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerNode;

public class ServerPeer {

	public static void main(String[] args) throws RemoteException {
		ServerNode server1 = new ServerNode(2356);
		NodeAddress server = distributed.systems.launchers.Server.discoverServer("127.0.0.1", 2355);
		System.out.println("address: " + server);
		server1.connect(server);
	}
}
