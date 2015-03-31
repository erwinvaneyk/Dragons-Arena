package distributed.systems.gametrace;

import distributed.systems.network.ServerNode;
import distributed.systems.network.logging.LogNode;
import distributed.systems.network.logging.Logger;

import java.rmi.RemoteException;

/**
 * Created by mashenjun on 31-3-15.
 */
public class workload {
    public static void main(String[] args) throws RemoteException {
        // 5 Server Nodes
        LogNode logger = new LogNode(2347, Logger.getDefault());
        ServerNode server1 = new ServerNode(2345);
        server1.startCluster();
        logger.connect(server1.getAddress());
        ServerNode server2 = new ServerNode(1235);
        server2.connect(server1.getAddress());
        ServerNode server3 = new ServerNode(1236);
        server3.connect(server1.getAddress());
        ServerNode server4 = new ServerNode(1237);
        server4.connect(server1.getAddress());
        ServerNode server5 = new ServerNode(1238);
        server5.connect(server1.getAddress());

        //24 Units


    }
}
