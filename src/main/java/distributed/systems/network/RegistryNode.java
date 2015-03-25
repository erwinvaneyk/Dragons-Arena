package distributed.systems.network;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import lombok.Getter;

public class RegistryNode implements Runnable {

	public static final int PORT = 1234;

	@Getter
	private final Registry registry;

	public static void main(String[] args) throws RemoteException {
		new RegistryNode(PORT).run();
	}

	public RegistryNode(int port) throws RemoteException {
		System.out.println("Creating registry...");
		registry = LocateRegistry.createRegistry(port);
		System.out.println("Registry running at port " + port + "...");
	}

	public RegistryNode(Address address) throws RemoteException {
		registry = LocateRegistry.getRegistry(address.getIp(), address.getPort());
	}

	@Override
	public void run() {
		try {
			// Don't terminate after starting the server
			Object lockObject = new Object();
			synchronized (lockObject) {
				lockObject.wait();
			}
		}
		catch (InterruptedException ignored) {}
		finally {
			System.out.println("Registry stopped.");
		}
	}
}
