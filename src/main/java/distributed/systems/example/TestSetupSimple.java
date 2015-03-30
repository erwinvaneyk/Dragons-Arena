package distributed.systems.example;

import java.rmi.RemoteException;

import distributed.systems.core.Socket;
import distributed.systems.network.Address;
import distributed.systems.network.DragonNode;
import distributed.systems.network.LocalSocket;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeType;
import distributed.systems.network.PlayerNode;
import distributed.systems.network.ServerNode;
import distributed.systems.network.logging.LogNode;
import distributed.systems.network.logging.Logger;

import java.rmi.RemoteException;

public class TestSetupSimple {

	public static void main(String[] args) throws RemoteException {
		ServerNode server1 = new ServerNode(2345);
		server1.connect(new NodeAddress(NodeType.SERVER, 1, new Address("127.0.0.1", 7890)));
		//server1.startCluster();
		/*ServerNode server2 = new ServerNode(2346);
		server2.connect(server1.getServerAddress());
		// Players
		PlayerNode player1 = new PlayerNode(server1.getAddress(),10,10);
		PlayerNode player2 = new PlayerNode(server1.getAddress(),15,15);
		LogNode logger = new LogNode(2347, Logger.getDefault());
		logger.connect(server1.getAddress());
		DragonNode dragonNode = new DragonNode(server1.getAddress(), 2,3);

		server1.launchViewer();
        //server2.launchViewer();
		System.out.println("ALL GOOD!!!!");*/
	}
}
