package distributed.systems.core;

import distributed.systems.das.BattleField;
import distributed.systems.das.units.Unit;
import distributed.systems.example.LogHandler;
import distributed.systems.example.NodeAddress;
import distributed.systems.example.ServerNode;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

public class SynchronizedSocket implements Socket,Serializable{

	private final Socket local;

	public SynchronizedSocket(Socket local) {
		this.local = local;
	}

	@Override
	public void register(String serverid) {
		this.local.register(serverid);
	}

	@Override
	public void addMessageReceivedHandler(BattleField battleField) {
		this.local.addMessageReceivedHandler(battleField);
	}

	@Override
	public void addMessageReceivedHandler(Unit unit) {
		this.local.addMessageReceivedHandler(unit);
	}

	@Override
	public void addMessageReceivedHandler(ServerNode server) {
		this.local.addMessageReceivedHandler(server);
	}

	@Override
	public void addLoggingReceivedHandler(LogHandler logger) {
		this.local.addLoggingReceivedHandler(logger);
	}

	@Override
	public void sendMessage(Message reply, String origin) {
		this.local.sendMessage(reply, origin);
	}

	@Override
	public void unRegister() {
		this.local.unRegister();
	}

	@Override
	public List<NodeAddress> getNodes() throws RemoteException {
		return this.local.getNodes();
	}
}
