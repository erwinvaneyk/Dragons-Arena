package distributed.systems.example;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.BattleField;
import distributed.systems.das.units.Unit;

public class LocalSocket implements Socket {

	private String id;

	private final Registry registry;

	private static final String PROTOCOL = "localsocket://";

	public LocalSocket() {
		try {
			registry = LocateRegistry.getRegistry("127.0.0.1", RegistryHandler.PORT);
		} catch (RemoteException e) {
			throw new RuntimeException("Mweh!");
		}
	}

	@Override
	public void register(String id) {
		this.id = id;
	}

	@Override
	public void addMessageReceivedHandler(BattleField battleField) {
		try {
			registry.bind(id, battleField);
		}
		catch (AlreadyBoundException e) {
			throw new AlreadyAssignedIDException();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addMessageReceivedHandler(Unit unit) {
		try {
			registry.bind(id, unit);
		}
		catch (AlreadyBoundException e) {
			throw new AlreadyAssignedIDException();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void sendMessage(Message message, String destination) {
		System.out.println(message);

		try {
			message.put("origin", PROTOCOL + this.id);
			IMessageReceivedHandler handler = (IMessageReceivedHandler) registry.lookup(destination.substring(PROTOCOL.length()));
			handler.onMessageReceived(message);
		}
		catch (NotBoundException e) {
			e.printStackTrace();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unRegister() {
		try {
			registry.unbind(id);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		catch (NotBoundException e) {
			e.printStackTrace();
		}
	}
}
