package distributed.systems.network.messagehandlers;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import distributed.systems.core.LogType;
import distributed.systems.core.Message;
import distributed.systems.das.BattleField;
import distributed.systems.network.ConnectionException;
import distributed.systems.network.NodeType;
import distributed.systems.network.ServerNode;
import org.apache.commons.lang.SerializationUtils;

public class SyncBattlefieldHandler implements MessageHandler {

	public static final String MESSAGE_TYPE = "SYNC_BATTLEFIELD";

	private final ServerNode me;

	public SyncBattlefieldHandler(ServerNode me) {
		this.me = me;

	}

	public static BattleField syncBattlefield(ServerNode me) {
		Message message = me.getMessageFactory().createMessage(MESSAGE_TYPE);

		List<Message> battlefields = me.getConnectedNodes().stream()
				.filter(node -> node.getAddress().getType().equals(NodeType.SERVER))
				.flatMap(server -> {
					me.getServerSocket().logMessage("Requesting battlefield from `" + server + "`.", LogType.DEBUG);
					try {
						return Stream.of(me.getServerSocket().sendMessage(message, server.getAddress()));
					}
					catch (RuntimeException | ConnectionException e) {
						e.printStackTrace();
						me.getServerSocket().logMessage(
								"Failed to send message to node `" + server + "`; message: " + message + ", because: "
										+ e, LogType.ERROR);
						return Stream.empty();
					}
				}).collect(toList());
		me.getServerSocket().logMessage("Syncing battlefield, received " + battlefields.size() + " battlefields.",
				LogType.DEBUG);


		// Election (byzantine)
		BattleField battleField = battlefields.stream()
				.map(node -> (BattleField) node.get("battlefield"))
				.collect(Collectors.groupingBy(w -> w, Collectors.counting()))
				.entrySet()
				.stream()
				.reduce((a, b) -> a.getValue() > b.getValue() ? a : b)
				.map(Map.Entry::getKey)
				.get();
		battleField.setMessagefactory(me.getMessageFactory());
		battleField.setServerSocket(me.getServerSocket());
		me.getServerSocket().logMessage(
				"Synced battlefield, choose battlefield with hash `" + battleField.hashCode() + "`", LogType.DEBUG);
		return battleField;
	}


	public Message onMessageReceived(Message message) {
		// TODO: desyncronized battlefields fix
		Message response = me.getMessageFactory().createMessage(MESSAGE_TYPE)
				.put("battlefield", (Serializable) SerializationUtils.clone(me.getServerState().getBattleField()));
		return response;
	}

	@Override
	public String getMessageType() {
		return MESSAGE_TYPE;
	}
}
