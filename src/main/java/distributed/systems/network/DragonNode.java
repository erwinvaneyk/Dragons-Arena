package distributed.systems.network;


import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import distributed.systems.core.LogType;
import distributed.systems.core.MessageFactory;
import distributed.systems.das.units.Dragon;
import distributed.systems.das.units.Unit;
import distributed.systems.network.messagehandlers.ClientGameActionHandler;
import distributed.systems.network.services.ClientHeartbeatService;
import distributed.systems.network.services.HeartbeatService;
import distributed.systems.network.services.NodeBalanceService;

import lombok.Getter;



public class DragonNode extends AbstractNode implements ClientNode, Serializable {

	private List<ServerAddress> knownServers = new ArrayList<>();

	@Getter
	private NodeAddress serverAddress;
	private HeartbeatService heartbeatService;
	private Dragon dragon;



	public static void main(String[] args) throws RemoteException {
		new DragonNode(null, 1, 1);
	}




	public DragonNode(NodeAddress server, int x, int y) throws RemoteException {
		// Setup
		address = new NodeAddress(-1, NodeAddress.NodeType.DRAGON);
		serverAddress = server;
		messageFactory = new MessageFactory(address);

		// Join server
		serverAddress = NodeBalanceService.joinServer(this, serverAddress);
		socket = LocalSocket.connectTo(serverAddress);
		socket.register(address);
		socket.addMessageReceivedHandler(this);
		knownServers.add(new ServerAddress(serverAddress));
		heartbeatService = new ClientHeartbeatService(this, socket).expectHeartbeatFrom(knownServers);

		// Add message handlers
		addMessageHandler(heartbeatService);
		addMessageHandler(new ClientGameActionHandler(this));

		// TODO: get reserve servers

		// Setup heartbeat service
		runService(heartbeatService);

		// spawn dragon

		this.dragon = new Dragon(x,y, this);
		socket.logMessage("Dragon (" + address + ") created and running. Assigned to server: " + serverAddress, LogType.INFO);
		dragon.start();
	}

	@Override

	public Unit getUnit() {
		return dragon;
	}

	@Override
	public NodeAddress.NodeType getNodeType() {
		return NodeAddress.NodeType.DRAGON;
	}
}
