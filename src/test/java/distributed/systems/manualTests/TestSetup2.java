package distributed.systems.manualTests;

import distributed.systems.network.ConnectionException;
import distributed.systems.network.DragonNode;
import distributed.systems.network.PlayerNode;
import distributed.systems.network.ServerNode;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;

@Slf4j
public class TestSetup2 {
    public static void main(String[] args) throws RemoteException, InterruptedException, ConnectionException {
        // Server setup
        ServerNode server1 = new ServerNode(1234);
	    server1.startCluster();
        //ServerNode server2 = new ServerNode(1235, false);
        //ServerNode server3 = new ServerNode(1236, false);
        //server3.getServerSocket().connectToCluster(server2.getAddress());*/
        //server2.connect(server1.getServerAddress());
        DragonNode dragonNode = new DragonNode(server1.getAddress(),10,10);
        // Players added
        PlayerNode player1 = new PlayerNode(server1.getAddress(),5,5);
        PlayerNode player2 = new PlayerNode(server1.getAddress(),4,4);
        //DragonNode dragonNode = new DragonNode(server1.getAddress(),0,0);
        //System.out.println(server1.getBattlefield().equals(server2.getBattlefield()));
        System.out.println(server1.getAddress());
        //System.out.println(server2.getAddress());
        System.out.println(player1.getAddress());
        System.out.println(player2.getAddress());
        server1.launchViewer();
        //server2.launchViewer();
    }
}
