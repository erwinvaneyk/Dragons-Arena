package distributed.systems.core;

import distributed.systems.das.BattleField;
import distributed.systems.das.units.Unit;

public interface Socket {

	public void register(String serverid);

	public void addMessageReceivedHandler(BattleField battleField);

	public void addMessageReceivedHandler(Unit unit);

	public void sendMessage(Message reply, String origin);

	public void unRegister();

}
