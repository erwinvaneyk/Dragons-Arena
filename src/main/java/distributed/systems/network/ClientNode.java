package distributed.systems.network;

import java.io.Serializable;


import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.das.units.Unit;
import distributed.systems.network.services.HeartbeatService;


public interface ClientNode extends Serializable {

	public NodeAddress getServerAddress();

	public NodeAddress getAddress();

	public ExtendedSocket getSocket();

	public MessageFactory getMessageFactory();

	public Unit getUnit();

	public Message sendMessageToServer(Message message);

	public PlayerState getPlayerState();

	public void joinServer(NodeAddress server) throws ConnectionException;

	public HeartbeatService getHeartbeatService();
}

