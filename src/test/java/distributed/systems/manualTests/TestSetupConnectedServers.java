package distributed.systems.manualTests;

import java.rmi.RemoteException;

import distributed.systems.network.ConnectionException;
import distributed.systems.network.ServerNode;
import distributed.systems.network.logging.LogNode;
import distributed.systems.network.logging.Logger;

public class TestSetupConnectedServers {

	public static void main(String[] args) throws RemoteException, ConnectionException {
		LogNode logger = new LogNode(2351, Logger.getDefault());
		LogNode logger2 = new LogNode(2352, Logger.getDefault());
		ServerNode server1 = new ServerNode(2345);
		//server1.connect(new NodeAddress(NodeType.SERVER, 2, new Address("127.0.0.1",12)));
		server1.startCluster();
		logger.connect(server1.getAddress());
		//logger.connect(server1.getAddress());
		ServerNode server2 = new ServerNode(2346);
		server2.connect(server1.getAddress());
		ServerNode server3 = new ServerNode(2347);
		server3.connect(server1.getAddress());
		ServerNode server4 = new ServerNode(2348);
		server4.connect(server3.getAddress());
		logger2.connect(server2.getAddress());
		System.out.println("Server 0: " + server1.getNodeState() + " -> " + server1.getConnectedNodes().size());
		System.out.println("logger 1: " + logger.getNodeState() + " -> " + logger.getConnectedNodes().size());
		System.out.println("Server 1: " + server2.getNodeState() + " -> " + server2.getConnectedNodes().size());
		System.out.println("Server 2: " + server3.getNodeState() + " -> " + server3.getConnectedNodes().size());
		System.out.println("Server 3: " + server4.getNodeState() + " -> " + server4.getConnectedNodes().size());
		//Players*/
		//PlayerNode player1 = new PlayerNode(server1.getAddress(),10,10);
		//PlayerNode player2 = new PlayerNode(server1.getAddress(),15,15);
		//DragonNode dragonNode = new DragonNode(server1.getAddress(), 2,3);

		//server1.launchViewer();
        //server2.launchViewer();
		System.out.println("ALL GOOD!!!!");
	}
}