package distributed.systems.network.messagehandlers;

import java.rmi.RemoteException;

import distributed.systems.core.Message;
import distributed.systems.network.ClientNode;
import distributed.systems.network.ConnectionException;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.services.NodeBalanceService;

public class ClientNodeBalanceHandler implements MessageHandler {

	private final ClientNode me;

	@Override
	public String getMessageType() {
		return NodeBalanceService.CLIENT_JOIN;
	}

	public ClientNodeBalanceHandler(ClientNode me) {
		this.me = me;
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		NodeAddress newServer = (NodeAddress) message.get("newServer");
		try {
			me.joinServer(newServer);
		}
		catch (ConnectionException e) {
			return null;
		}
		return me.getMessageFactory().createMessage(NodeBalanceService.CLIENT_JOIN)
				.put("action", "move")
				.put("success", true);
	}
}
