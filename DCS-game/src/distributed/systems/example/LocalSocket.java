package distributed.systems.example;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.das.BattleField;
import distributed.systems.das.units.Unit;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * Created by mashenjun on 3-3-15.
 */
public class LocalSocket implements Socket {
    //may be a dict?
    private IMessageReceivedHandler handler;
    private String ID;


    @Override
    public synchronized void register(String serverid)  {

        Registry registry = null;
        ID = serverid;
        try {
            registry = LocateRegistry.getRegistry();
            registry.bind(serverid,this);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void addMessageReceivedHandler(BattleField battleField){
        this.handler= battleField;
    }

    @Override
    public void addMessageReceivedHandler(Unit unit) {
        this.handler=unit;
    }

    @Override
    public void sendMessage(Message reply, String origin) {
        //call the Battle field onMessageReceiver.
        try {
            Registry registry = LocateRegistry.getRegistry();
            Socket Rsocket =(Socket) registry.lookup(origin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unRegister() {
        try {
            Registry registry = LocateRegistry.getRegistry();
            registry.unbind(this.ID);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
