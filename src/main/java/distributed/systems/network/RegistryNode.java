package distributed.systems.network;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class RegistryNode implements Runnable {

	public static final int PORT = 1234;

	public static void main(String[] args) {
		new RegistryNode(PORT).run();
	}

	public RegistryNode(int port) {
		try {
			System.out.println("Creating registry...");
			LocateRegistry.createRegistry(port);
			System.out.println("Registry running at port " + port + "...");

		} catch (RemoteException e) {
			e.printStackTrace();
		}
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
