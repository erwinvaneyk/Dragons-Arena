package distributed.systems.network;

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import lombok.Getter;

public class RegistryNode implements Runnable {

	public static final int PORT = 1234;

	@Getter
	private final Registry registry;
	private final Address address;

	public static void main(String[] args) throws RemoteException {
		new RegistryNode(PORT).run();
	}

	public RegistryNode(int port) throws RemoteException {
		registry = LocateRegistry.createRegistry(port);
		this.address = Address.getMyAddress(port);
	}

	public RegistryNode(Address address) throws RemoteException {
		registry = LocateRegistry.getRegistry(address.getIp(), address.getPort());
		this.address = address;
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

	public String toString() {
		try {
			return Arrays.toString(registry.list());
		}
		catch (RemoteException e) {
			e.printStackTrace();
			return "[]";
		}
	}

	public void disconnect() {
		JMXServiceURL url;
		String urlString = "service:jmx:rmi://localhost:" + (address.getPort() + 1) + "/jndi/rmi://:" + address.getPort() + "/jmxrmi";
		try {
			url = new JMXServiceURL(urlString);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Malformed service url created " + urlString, e);
		}

		JMXConnectorServer connector;
		try {
			connector = JMXConnectorServerFactory.newJMXConnectorServer(url,
					new HashMap<>(),
					ManagementFactory.getPlatformMBeanServer());
			// close the connection
			if (connector != null) {
				connector.stop();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		try {
			UnicastRemoteObject.unexportObject(registry, true);
		}
		catch (NoSuchObjectException e) {
			e.printStackTrace();
		}
	}
}
