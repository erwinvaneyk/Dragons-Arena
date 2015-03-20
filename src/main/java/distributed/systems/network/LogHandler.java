package distributed.systems.network;


import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;

public class LogHandler extends UnicastRemoteObject implements IMessageReceivedHandler, Serializable {

	private transient final ObjectMapper mapper = new ObjectMapper();
	//private transient final Node node;

	public static void main(String[] args) throws RemoteException, JsonProcessingException {
		new LogHandler("elasticsearch");
	}

	protected LogHandler(String clustername) throws RemoteException, JsonProcessingException {
		/*node = NodeBuilder.nodeBuilder().clusterName(clustername).node();
		Message mess = new Message();
		mess.put("keytest","valuetest");
		mapper.setDateFormat(new SimpleDateFormat());
		onMessageReceived(mess);*/
	}

	@Override
	public void onMessageReceived(Message message) throws RemoteException {
		// log message
		try {
			String json = mapper.writeValueAsString(message);
			/*node.client()
					.prepareIndex("IN4391", "logging")
					.setSource(json)
					.execute();*/
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
