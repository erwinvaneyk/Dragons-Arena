package distributed.systems.launchers;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import distributed.systems.network.Address;
import distributed.systems.network.ConnectionException;
import distributed.systems.network.LocalSocket;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.NodeType;
import distributed.systems.network.PlayerNode;
import distributed.systems.network.ServerNode;

public class Server {

	/**
	 * arg[0] = ip of server
	 * arg[1] = port of server
	 */
	public static void main(String[] args) throws RemoteException, ConnectionException {
		securitySetup();
		if(args.length < 1) {
			throw new RuntimeException("Invalid format requires the arguments: <myport> <ip> <port> (connect to cluster) or <myport> (create cluster)");
		}
		ServerNode server1 = new ServerNode(Integer.valueOf(args[0]));
		if(args.length == 1) {
			System.out.println("Creating cluster on port " + args[0]);
			server1.startCluster();
		} else {
			System.out.println("Connecting to cluster on " + args[1] + ":" + args[2]);
			server1.connect(discoverServer(args[1], Integer.valueOf(args[2])));
		}

		for(String arg : args) {
			if(arg.equalsIgnoreCase("viewer")) {
				System.out.println("Launching viewer");
				server1.launchViewer();
			}
		}
	}

	public static NodeAddress discoverServer(String ip, int port) throws RemoteException, ConnectionException {
		LocalSocket socket = LocalSocket.connectTo(ip, port);
		String[] bindings = socket.getRegistry().list();
		for(String binding : bindings) {
			NodeAddress nodeAddress = NodeAddress.fromAddress(binding);
			if(nodeAddress.getType().equals(NodeType.SERVER)) {
				nodeAddress.setPhysicalAddress(new Address(ip, port));
				return nodeAddress;
			}
		}
		throw new RuntimeException("No server found on the provided location: " + ip + ":" + port);
	}

	public static void securitySetup() {
		System.setProperty("java.security.policy","file:./my.policy");
		// Create and install a security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
	}
}
