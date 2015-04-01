package distributed.systems.launchers;

import java.rmi.RemoteException;

import distributed.systems.network.NodeAddress;
import distributed.systems.network.PlayerNode;

public class Player {

	/**
	 * arg[0] = ip of server
	 * arg[1] = port of server
	 */
	public static void main(String[] args) throws RemoteException {
		if(args.length < 2) {
			throw new RuntimeException("Invalid format requires the arguments: <ip> <port>");
		}
		NodeAddress nodeAddress = Server.discoverServer(args[0], Integer.valueOf(args[1]));
		new PlayerNode(nodeAddress);
	}
}
