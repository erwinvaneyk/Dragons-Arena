package distributed.systems.network;


import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.das.units.Dragon;
import distributed.systems.das.units.Unit;
import distributed.systems.das.units.impl.StationaryDragon;
import distributed.systems.network.messagehandlers.ClientGameActionHandler;
import distributed.systems.network.services.ClientHeartbeatService;
import distributed.systems.network.services.HeartbeatService;
import distributed.systems.network.services.NodeBalanceService;

import lombok.Getter;



public class DragonNode extends AbstractNode implements ClientNode, Serializable {

	private List<NodeAddress> knownServers = new ArrayList<>();
	@Getter
	private NodeAddress address;

	@Getter
	private NodeAddress serverAddress;
	private HeartbeatService heartbeatService;
	private Dragon dragon;

	public static void main(String[] args) throws RemoteException {
		new DragonNode(null, 1, 1);
	}

	public DragonNode(NodeAddress server, int x, int y) throws RemoteException {
		// Setup
		address = new NodeAddress(-1, NodeType.DRAGON);
		serverAddress = server;
		messageFactory = new MessageFactory(new NodeState(address, NodeType.DRAGON));

		// Join server
		serverAddress = NodeBalanceService.joinServer(this, serverAddress);
		socket = LocalSocket.connectTo(serverAddress);
		socket.register(address);
		socket.addMessageReceivedHandler(this);
		knownServers.add(serverAddress);
		heartbeatService = new ClientHeartbeatService(this, socket).expectHeartbeatFrom(knownServers);

		// Add message handlers
		addMessageHandler(heartbeatService);
		addMessageHandler(new ClientGameActionHandler(this));

		// TODO: get reserve servers

		// Setup heartbeat service
		runService(heartbeatService);

		// spawn dragon

		this.dragon = new StationaryDragon(x,y, this);
		socket.logMessage("Dragon (" + address + ") created and running. Assigned to server: " + serverAddress, LogType.INFO);
		dragon.start();
	}

	@Override

	public Unit getUnit() {
		return dragon;
	}

	@Override
	public Message sendMessageToServer(Message message) {
		return socket.sendMessage(message, this.getServerAddress());
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.DRAGON;
	}
}
