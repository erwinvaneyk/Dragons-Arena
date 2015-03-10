package distributed.systems.example;

import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.das.BattleField;
import distributed.systems.das.units.Unit;

public class LocalSocket implements Socket {

	@Override
	public void register(String serverid) {

	}

	@Override
	public void addMessageReceivedHandler(BattleField battleField) {

	}

	@Override
	public void addMessageReceivedHandler(Unit unit) {

	}

	@Override
	public void sendMessage(Message reply, String origin) {

	}

	@Override
	public void unRegister() {

	}
}
