package distributed.systems.example;

import java.rmi.RemoteException;

import distributed.systems.network.DragonNode;
import distributed.systems.network.PlayerNode;
import distributed.systems.network.RegistryNode;
import distributed.systems.network.ServerNode;
import distributed.systems.network.logging.LogNode;
import distributed.systems.network.logging.Logger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestSetup {

	public static void main(String[] args) throws RemoteException {
		new Thread(new RegistryNode(RegistryNode.PORT)).start();
		new ServerNode();
		new LogNode(Logger.getDefault());
		new PlayerNode(1,2);
		new PlayerNode(3,2);
		new PlayerNode(6,6);
		DragonNode dragon = new DragonNode(10, 10);
	}
}
