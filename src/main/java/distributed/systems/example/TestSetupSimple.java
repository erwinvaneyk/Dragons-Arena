package distributed.systems.example;

import distributed.systems.network.DragonNode;
import distributed.systems.network.PlayerNode;
import distributed.systems.network.ServerNode;

import java.rmi.RemoteException;

public class TestSetupSimple {

	public static void main(String[] args) throws RemoteException {
		//LogNode logger = new LogNode(2347, Logger.getDefault());
		ServerNode server1 = new ServerNode(2345);
		//logger.connect(server1.getAddress());
		//server1.connect(new NodeAddress(NodeType.SERVER, 2, new Address("127.0.0.1",12)));
		server1.startCluster();
		//logger.connect(server1.getAddress());
		ServerNode server2 = new ServerNode(2346);
		server2.connect(server1.getAddress());
		/*ServerNode server3 = new ServerNode(2347);
        server3.launchViewer();
        //server2.launchViewer();
		System.out.println("ALL GOOD!!!!");
		server1.moveClient(player1.getAddress(), server2.getAddress());*/
	}
}