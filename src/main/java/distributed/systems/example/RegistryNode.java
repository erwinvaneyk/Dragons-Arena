package distributed.systems.example;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class RegistryNode {

	public static final int PORT = 1234;

	public static void main(String[] args) {
		try {
			System.out.println("Creating registry...");
			LocateRegistry.createRegistry(PORT);
			System.out.println("Registry running at port " + PORT + "...");

			// Don't terminate after starting the server
			Object lockObject = new Object();
			synchronized(lockObject){
				lockObject.wait();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		catch (InterruptedException ignored) {}
		finally {
			System.out.println("Registry stopped.");
		}
	}
}
