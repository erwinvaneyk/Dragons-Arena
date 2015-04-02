package distributed.systems.example.distributed;

import java.rmi.RemoteException;

import distributed.systems.network.Address;
import distributed.systems.network.ConnectionException;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeType;
import distributed.systems.network.PlayerNode;

public class Player {

	public static void main(String[] args) throws RemoteException, ConnectionException {
		NodeAddress server1 = new NodeAddress(NodeType.SERVER, 0, new Address("localhost", 2355));
		PlayerNode player1 = new PlayerNode(server1 ,0,0);
	}
}
