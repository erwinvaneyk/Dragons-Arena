package distributed.systems.network;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import distributed.systems.core.Message;
import lombok.Getter;

public class RegistryNode implements Runnable {

	public static final int PORT = 1234;

	private final List<RegistryNode> otherNodes = new ArrayList<>();

	private final Address address = Address.getMyAddress(1000);

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
		registry = LocateRegistry.getRegistry(address.getIp().toString(), address.getPort());
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

	public void sendMessage(Message message, String destination) {

	}

}
