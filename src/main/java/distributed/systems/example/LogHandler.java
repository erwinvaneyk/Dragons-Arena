package distributed.systems.example;


import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import distributed.systems.core.IMessageProxyHandler;
import distributed.systems.core.Message;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

public class LogHandler extends UnicastRemoteObject implements IMessageProxyHandler, Serializable {

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
