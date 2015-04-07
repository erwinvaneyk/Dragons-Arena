package distributed.systems.manualTests;

import java.rmi.RemoteException;

import distributed.systems.network.ConnectionException;
import distributed.systems.network.DragonNode;
import distributed.systems.network.PlayerNode;
import distributed.systems.network.RegistryNode;
import distributed.systems.network.ServerNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestSetup {
    public static final int MIN_PLAYER_COUNT = 30;
    public static final int MAX_PLAYER_COUNT = 60;
    public static final int DRAGON_COUNT = 20;
    public static final int TIME_BETWEEN_PLAYER_LOGIN = 5000;
    public static int playerCount;

	public static void main(String[] args) throws RemoteException, ConnectionException {
		new Thread(new RegistryNode(RegistryNode.PORT)).start();
		ServerNode server = new ServerNode(RegistryNode.PORT);
		server.startCluster();
		//new LogNode(Logger.getDefault());
		new PlayerNode(server.getAddress(),1,2);
		new PlayerNode(server.getAddress(),3,2);
		new PlayerNode(server.getAddress(),6,6);
		DragonNode dragon = new DragonNode(server.getAddress(),10, 10);

	}
}
