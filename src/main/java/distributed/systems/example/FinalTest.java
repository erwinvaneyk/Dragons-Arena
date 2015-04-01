package distributed.systems.example;

import java.rmi.RemoteException;

import distributed.systems.das.units.Dragon;
import distributed.systems.network.DragonNode;
import distributed.systems.network.PlayerNode;
import distributed.systems.network.ServerNode;

public class FinalTest {

	public static void main(String[] args) throws RemoteException {
		ServerNode server1 = new ServerNode(2345);
		ServerNode server2 = new ServerNode(2346);
		ServerNode server3 = new ServerNode(2347);
		ServerNode server4 = new ServerNode(2348);
		ServerNode server5 = new ServerNode(2349);


		server1.startCluster();
		server2.connect(server1.getAddress());
		server3.connect(server1.getAddress());
		server4.connect(server1.getAddress());
		server5.connect(server1.getAddress());

		for(int i = 0; i < 100; i++) {
			new PlayerNode(server1.getAddress());
		}


		for(int i = 0; i < 20; i++) {
			new DragonNode(server1.getAddress());
		}

		server1.launchViewer();

	}
}
