package distributed.systems.launchers;

import java.rmi.RemoteException;

import distributed.systems.network.Address;
import distributed.systems.network.LocalSocket;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeType;
import distributed.systems.network.ServerNode;

public class Logger {

	/**
	 * arg[0] = ip of server
	 * arg[1] = port of server
	 */
	public static void main(String[] args) throws RemoteException {
		if(args.length < 2) {
			throw new RuntimeException("Invalid format requires the arguments: <myport> <ip> <port> (connect to cluster)");
		}
		ServerNode server1 = new ServerNode(Integer.valueOf(args[0]));
		System.out.println("Connecting logger to cluster on " + args[1] + ":" + args[2]);
		server1.connect(Server.discoverServer(args[1], Integer.valueOf(args[2])));
	}
}
