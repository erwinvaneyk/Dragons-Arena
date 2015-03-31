package distributed.systems.gametrace;

import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.das.BattleField;
import distributed.systems.das.GameState;
import distributed.systems.das.MessageRequest;
import distributed.systems.network.DragonNode;
import distributed.systems.network.PlayerNode;
import distributed.systems.network.ServerNode;

import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by mashenjun on 31-3-15.
 */
public class workload {
    public static final int MIN_PLAYER_COUNT = 20;
    public static final int MAX_PLAYER_COUNT = 100;
    public static final int MIN_DRAGON_COUNT = 4;
    public static final int MAX_DRAGON_COUNT = 20;
    public static final int TIME_BETWEEN_PLAYER_LOGIN = 10000;
    public static int playerCount;
    public static Integer UnitCount = 0;
    public static Date timestamp;
    public static ArrayList Serverlist = new ArrayList<ServerNode>();

    public static void main(String[] args) throws RemoteException {

        Timer timer = new Timer();

        // 5 Server Nodes
        //LogNode logger = new LogNode(2347, Logger.getDefault());
        ServerNode server1 = new ServerNode(2345);
        server1.startCluster();
        Serverlist.add(server1);
        //logger.connect(server1.getAddress());
        ServerNode server2 = new ServerNode(1235);
        server2.connect(server1.getAddress());
        Serverlist.add(server2);
        ServerNode server3 = new ServerNode(1236);
        server3.connect(server1.getAddress());
        Serverlist.add(server3);
        ServerNode server4 = new ServerNode(1237);
        server4.connect(server1.getAddress());
        Serverlist.add(server4);
        ServerNode server5 = new ServerNode(1238);
        server5.connect(server1.getAddress());
        Serverlist.add(server5);
        server1.launchViewer();
        server2.launchViewer();
        //new DragonNode(server1.getAddress(), 10, 10);

        //24 Units
        for (int i = 0; i < MIN_DRAGON_COUNT; i++) {
			/* Try picking a random spot */
            int x, y, attempt = 0;
            final int temp = i;
            do {
                x = (int) (Math.random() * BattleField.MAP_WIDTH);
                y = (int) (Math.random() * BattleField.MAP_HEIGHT);
                attempt++;
            } while (server1.getServerState().getBattleField().getUnit(x, y).isPresent() && attempt < 10);
            System.out.println("try to add a dragon");
            // If we didn't find an empty spot, we won't add a new dragon
            if (server1.getServerState().getBattleField().getUnit(x, y).isPresent()) break;
            System.out.println("try to add a dragon");
            final int finalX = x;
            final int finalY = y;
                        new DragonNode(server1.getAddress(), finalX, finalY);



        }

        for (int i = 0; i < MIN_PLAYER_COUNT; i++) {
			/* Try picking a random spot */
            int x, y, attempt = 0;
            final int temp = i;
            do {
                x = (int) (Math.random() * BattleField.MAP_WIDTH);
                y = (int) (Math.random() * BattleField.MAP_HEIGHT);
                attempt++;
            } while (server1.getServerState().getBattleField().getUnit(x, y).isPresent() && attempt < 10);

            // If we didn't find an empty spot, we won't add a new dragon
            if (server1.getServerState().getBattleField().getUnit(x, y).isPresent()) break;

            final int finalX = x;
            final int finalY = y;
            final int timeslot = i;
            new PlayerNode(server1.getAddress(), finalX, finalY);

        }


        // here we set the timestamp where all units are places on the board
        timestamp = new Date();

        // add units periodically
        timer.schedule(new TimerTask(){
            public void run(){
                int randtemp = randInt(0,4);
                if (dectincrease(getinterval(timestamp,new Date()))){
                    increaseUnit((ServerNode) Serverlist.get(randtemp));
                }
                else {
                    decrease((ServerNode) Serverlist.get(randtemp));
                }

            }},500,1000);
    }

    public static int randInt(int min, int max) {
        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    public static void increaseUnit(ServerNode server){
        int x, y, attempt = 0;
        do {
            x = (int) (Math.random() * BattleField.MAP_WIDTH);
            y = (int) (Math.random() * BattleField.MAP_HEIGHT);
            attempt++;
        } while (server.getServerState().getBattleField().getUnit(x, y).isPresent() && attempt < 10);

        // If we didn't find an empty spot, we won't add a new dragon
        if (server.getServerState().getBattleField().getUnit(x, y).isPresent()) return ;
        try {
            if(propertygenerate()==true) {
                System.out.println("add a player in the thread ");
                new PlayerNode(server.getAddress(), x, y);
            }
            else {
                System.out.println("add a dragon in the thread ");
                new DragonNode(server.getAddress(), x, y);}

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return ;
    }

    public static void decrease(ServerNode server){
        System.out.println("start the decrease");
        int x, y, attempt = 0;
//        do {
//            x = (int) (Math.random() * BattleField.MAP_WIDTH);
//            y = (int) (Math.random() * BattleField.MAP_HEIGHT);
//            attempt++;
//        } while (!server.getServerState().getBattleField().getUnit(x, y).isPresent() && attempt < 10);
//        if (!server.getServerState().getBattleField().getUnit(x, y).isPresent()) return ;
        System.out.println("kill a Unit in the workload"+server.getServerState().getBattleField().getUnits().size() );
        x= server.getServerState().getBattleField().getUnits().get(0).getX();
        y= server.getServerState().getBattleField().getUnits().get(0).getY();
        MessageFactory messageFactory= new MessageFactory(server.getServerState().getBattleField().getUnit(x,y).get().getPlayerState());
        Message workloadDamage = messageFactory.createMessage();
        workloadDamage.put("request", MessageRequest.workloaddamage);
        workloadDamage.put("tx",x);
        workloadDamage.put("ty",y);
        workloadDamage.put("damage",150);
        server.getServerState().getBattleField().onMessageReceived(workloadDamage);
        return ;
    }

    public static int getinterval(Date start, Date current){
        long resutle = current.getTime()-start.getTime();
                System.out.println("I want to know the interval result is "+resutle/(1000* GameState.GAME_SPEED));
        return (int) (resutle/(1000*GameState.GAME_SPEED));
    }

    public static boolean dectincrease(int interval){
        if(interval%20<=12){
            return true; //true is increase
        }
        else return false;
    }
    public static boolean propertygenerate(){
        int p = randInt(1,100);
        if(p>20){
            return true; // true is for player
        }
        else return false;
    }

}
