package distributed.systems.network.messagehandlers;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.network.NodeType;
import distributed.systems.network.ServerNode;

import java.rmi.RemoteException;

public class ServerGameActionHandler implements MessageHandler {


	private static final String MESSAGE_TYPE = "DEFAULT";
	private final ServerNode me;

	public ServerGameActionHandler(ServerNode me) {
		this.me = me;
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}

	@Override
	public Message onMessageReceived(Message message) throws RemoteException {
		// TODO: task distribution
		me.getSocket().logMessage("[" + me.getAddress() + "] received message: ("  + message + ")", LogType.DEBUG);

		Message response = me.getBattlefield().onMessageReceived(message);

        if (response!=null){
            System.out.println("currently, "+me.getAddress().getName()+" the other nodes are "+ response.toString());
            if(message.get("update").equals(true)){
                System.out.println("I send a update message handler "+ me.getAddress().getName()+" "+ this.me.getOtherNodes().toString());
                message.put("update", false);
                me.getServerSocket().broadcast(response, NodeType.SERVER);
            }
            else {
                System.out.println("I receive a update message handler "+ me.getAddress().getName());
            }

        }
        return response;
	}
}
