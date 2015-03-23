package distributed.systems.example;

/**
 * Created by mashenjun on 22-3-15.
 */

import java.rmi.RemoteException;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.BattleField;
import distributed.systems.das.units.Dragon;
import distributed.systems.das.units.Player;
import distributed.systems.network.DragonNode;
import distributed.systems.network.PlayerNode;
import distributed.systems.network.RegistryNode;
import distributed.systems.network.ServerNode;
import distributed.systems.network.logging.LogNode;
import distributed.systems.network.logging.Logger;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class TestSetup2 {
    public static final int MIN_PLAYER_COUNT = 30;
    public static final int MAX_PLAYER_COUNT = 60;
    public static final int DRAGON_COUNT = 20;
    public static final int TIME_BETWEEN_PLAYER_LOGIN = 5000;
    public static int playerCount;

    public static void main(String[] args) throws RemoteException {
        new Thread(new RegistryNode(RegistryNode.PORT)).start();
        ServerNode snode = new ServerNode();
        new LogNode(Logger.getDefault());
//        new PlayerNode(1,2);
//        new PlayerNode(3,2);
//        new PlayerNode(6,6);
        //DragonNode dragon = new DragonNode(10, 10);

        for(int i = 0; i < DRAGON_COUNT; i++) {
			/* Try picking a random spot */
            int x, y, attempt = 0;
            do {
                x = (int)(Math.random() * BattleField.MAP_WIDTH);
                y = (int)(Math.random() * BattleField.MAP_HEIGHT);
                attempt++;
            } while (snode.getBattlefield().getUnit(x, y) != null && attempt < 10);

            // If we didn't find an empty spot, we won't add a new dragon
            if (snode.getBattlefield().getUnit(x, y) != null) break;

            final int finalX = x;
            final int finalY = y;

            new DragonNode(finalX, finalY);
			/* Create the new dragon in a separate
			 * thread, making sure it does not
			 * block the system.
			 */
/*            new Thread(new Runnable() {
                public void run() {
                    try {
                        new DragonNode(finalX, finalY);
                    } catch (AlreadyAssignedIDException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();*/

        }

        /* Initialize a random number of players (between [MIN_PLAYER_COUNT..MAX_PLAYER_COUNT] */
        playerCount = (int)((MAX_PLAYER_COUNT - MIN_PLAYER_COUNT) * Math.random() + MIN_PLAYER_COUNT);
        for(int i = 0; i < playerCount; i++)
        {
			/* Once again, pick a random spot */
            int x, y, attempt = 0;
            do {
                x = (int)(Math.random() * BattleField.MAP_WIDTH);
                y = (int)(Math.random() * BattleField.MAP_HEIGHT);
                attempt++;
            } while (snode.getBattlefield().getUnit(x, y) != null && attempt < 10);

            // If we didn't find an empty spot, we won't add a new player
            if (snode.getBattlefield().getUnit(x, y) != null) break;

            final int finalX = x;
            final int finalY = y;
            new PlayerNode(finalX,finalY);
			/* Create the new player in a separate
			 * thread, making sure it does not
			 * block the system.
/*			 *//*
            new Thread(new Runnable() {
                public void run() {
                    try {
                        new PlayerNode(finalX, finalY);
                    } catch (AlreadyAssignedIDException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();*/

        }

    }
}
