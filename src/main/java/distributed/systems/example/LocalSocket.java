package distributed.systems.example;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

import distributed.systems.core.BattlefieldProxy;
import distributed.systems.core.IMessageProxyHandler;
import distributed.systems.core.Message;
import distributed.systems.core.UnitProxy;
import distributed.systems.core.Socket;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.BattleField;
import distributed.systems.das.units.Unit;

public class LocalSocket implements Socket,Serializable {

	private String id;

	private final Registry registry;

	private static final String PROTOCOL = "localsocket://";

	public LocalSocket() {
		try {
			registry = LocateRegistry.getRegistry("127.0.0.1", RegistryNode.PORT);
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
			System.out.println("the list is "+Arrays.toString(registry.list()));
			registry.bind(id, new BattlefieldProxy(battleField));
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
			registry.bind(id, new UnitProxy(unit));
		}
		catch (AlreadyBoundException e) {
			throw new AlreadyAssignedIDException();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void addLoggingReceivedHandler(LogHandler logger) {
		try {
			registry.bind(id, logger);
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

		try {
			message.setOriginId(this.id);
			message.put("origin", PROTOCOL + this.id);
			IMessageProxyHandler handler = (IMessageProxyHandler) registry.lookup(destination.substring(PROTOCOL.length()));
			handler.onMessageReceived(message);
			System.out.println("send: " + message);
		}
		catch (NotBoundException | RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unRegister() {
		try {
			registry.unbind(id);
			System.out.println("Unregistered binding " + id);
		}
		catch (RemoteException | NotBoundException e) {
			System.out.println(id + " was already unRegistered!");
		}
	}
}
