package distributed.systems.example;

import java.rmi.RemoteException;

import distributed.systems.das.BattleField;
import distributed.systems.network.DragonNode;
import distributed.systems.network.PlayerNode;
import distributed.systems.network.RegistryNode;
import distributed.systems.network.ServerNode;
import distributed.systems.network.logging.LogNode;
import distributed.systems.network.logging.Logger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestSetup {
    public static final int MIN_PLAYER_COUNT = 30;
    public static final int MAX_PLAYER_COUNT = 60;
    public static final int DRAGON_COUNT = 20;
    public static final int TIME_BETWEEN_PLAYER_LOGIN = 5000;
    public static int playerCount;

	public static void main(String[] args) throws RemoteException {
		new Thread(new RegistryNode(RegistryNode.PORT)).start();
		ServerNode snode = new ServerNode();
		new LogNode(Logger.getDefault());
        DragonNode dragon = new DragonNode(10, 10);
		new PlayerNode(9,10);

		new PlayerNode(6,6);
        new PlayerNode(6,7);
        new PlayerNode(6,8);
        new PlayerNode(6,9);
	}
}
