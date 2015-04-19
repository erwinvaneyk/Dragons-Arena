package reportTesting.faultTolerance;

import java.rmi.RemoteException;

import distributed.systems.network.ConnectionException;
import distributed.systems.network.DragonNode;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.PlayerNode;
import distributed.systems.network.ServerNode;
import distributed.systems.network.logging.LogNode;
import distributed.systems.network.logging.Logger;

public class ServerDisconnects {

	public static void main(String[] args) throws RemoteException, ConnectionException, InterruptedException {
		ServerNode headnode = new ServerNode(7654);
		headnode.startCluster();
		Thread.sleep(100);
		addLogger(6789, headnode.getAddress()).start();
		Thread.sleep(100);
		addServer(7641, headnode.getAddress()).start();
		Thread.sleep(100);
		Thread thread = addServer(7642, headnode.getAddress());
		thread.start();
		Thread.sleep(100);
		addServer(7643, headnode.getAddress()).start();
		Thread.sleep(100);
		addServer(7644, headnode.getAddress()).start();
		Thread.sleep(100);

		for(int i = 0; i < 20; i++) {
			addPlayerOrDragon(headnode.getAddress()).start();
			Thread.sleep(500);
		}
		headnode.launchViewer();

		Thread.sleep(10000);
		addServer(7645, headnode.getAddress()).start();
	}

	private static Thread addServer(int port, NodeAddress cluster) {
		return new Thread(() -> {
			try {
				new ServerNode(port).connect(cluster);
			}
			catch (ConnectionException | RemoteException e) {
				e.printStackTrace();
			}
		});
	}

	private static Thread addLogger(int port, NodeAddress cluster) {
		return new Thread(() -> {
			try {
				new LogNode(port, Logger.getDefault()).connect(cluster);
			}
			catch (ConnectionException | RemoteException e) {
				e.printStackTrace();
			}
		});
	}

	private static Thread addPlayerOrDragon(NodeAddress cluster) {
		if(Math.random() > 0.2) {
			return new Thread(() -> {
				try {
					new PlayerNode(cluster);
				}
				catch (ConnectionException | RemoteException e) {
					e.printStackTrace();
				}
			});
		} else {
			return new Thread(() -> {
				try {
					new DragonNode(cluster);
				}
				catch (ConnectionException | RemoteException e) {
					e.printStackTrace();
				}
			});
		}
	}
}
