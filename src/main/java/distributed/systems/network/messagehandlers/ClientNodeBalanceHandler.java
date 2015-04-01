package distributed.systems.network.messagehandlers;

import java.rmi.RemoteException;

import distributed.systems.core.Message;
import distributed.systems.network.ClientNode;
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
		System.out.println("t");
		NodeAddress newServer = (NodeAddress) message.get("newServer");
		me.joinServer(newServer);
		return me.getMessageFactory().createMessage(NodeBalanceService.CLIENT_JOIN)
				.put("action", "move")
				.put("success", true);
	}
}
