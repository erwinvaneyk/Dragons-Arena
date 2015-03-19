package distributed.systems.core;

import distributed.systems.das.BattleField;
import distributed.systems.das.units.Unit;
import distributed.systems.example.LogHandler;

public interface Socket {

	public void register(String id);

	public void addMessageReceivedHandler(BattleField battleField);

	public void addMessageReceivedHandler(Unit unit);

	public void addLoggingReceivedHandler(LogHandler logger);

	public void sendMessage(Message reply, String origin);

	public void unRegister();

}
