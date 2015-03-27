package distributed.systems.das;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.MessageFactory;
import distributed.systems.core.SynchronizedQueue;
import distributed.systems.das.units.Dragon;
import distributed.systems.das.units.Player;
import distributed.systems.das.units.Unit;
import distributed.systems.das.units.Unit.UnitType;
import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.core.exception.IDNotAssignedException;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerNode;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.Synchronized;

/**
 * The actual battlefield where the fighting takes place.
 * It consists of an array of a certain width and height.
 * 
 * It is a singleton, which can be requested by the 
 * getBattleField() method. A unit can be put onto the
 * battlefield by using the putUnit() method.
 * 
 * @author Pieter Anemaet, Boaz Pat-El
 */
@EqualsAndHashCode
public class BattleField implements Serializable, IMessageReceivedHandler {
	@Setter
	private transient MessageFactory messagefactory;
	/* The array of units */
	private Unit[][] map;
	/* Primary socket of the battlefield */
	@Setter
	private transient ExtendedSocket serverSocket;
	
	/* The last id that was assigned to an unit. This variable is used to
	 * enforce that each unit has its own unique id.
	 */
	private int lastUnitID = 0;
	public final static String serverID = "server";
	public final static int MAP_WIDTH = 25;
	public final static int MAP_HEIGHT = 25;
	private ArrayList<Unit> units;
    public transient LinkedList<Message> Lqueue;
    private transient Timer timer=new Timer();
	/**
	 * Initialize the battlefield to the specified size 
	 * @param width of the battlefield
	 * @param height of the battlefield
	 */	private BattleField(int width, int height) {
        map = new Unit[width][height];
        units = new ArrayList<>();
        Lqueue = new LinkedList<Message>();
        //control the Lqueue
        StartLexectueThread();
        //start a time task to clean the Lqueue in case tasks are block in the Lqueue
        StartReleaseThread();
    }

	public BattleField() {
		map = new Unit[MAP_WIDTH][MAP_HEIGHT];
		units = new ArrayList<>();
		Lqueue = new LinkedList<Message>();
		//control the Lqueue
		StartLexectueThread();
		//start a time task to clean the Lqueue in case tasks are block in the Lqueue
		StartReleaseThread();
	}

	/**
	 * Puts a new unit at the specified position. First, it
	 * checks whether the position is empty, if not, it
	 * does nothing.
	 * In addition, the unit is also put in the list of known units.
	 * 
	 * @param unit is the actual unit being spawned 
	 * on the specified position.
	 * @param x is the x position.
	 * @param y is the y position.
	 * @return true when the unit has been put on the 
	 * specified position.
	 */
	private boolean spawnUnit(Unit unit, int x, int y)
	{
        synchronized (this) {
			if (map[x][y] != null)
				return false;
			map[x][y] = unit;
			unit.setPosition(x, y);
		}
		units.add(unit);
        map[x][y].setAdjacent(adjacent(x,y));
		return true;
	}

	/**
	 * Put a unit at the specified position. First, it
	 * checks whether the position is empty, if not, it
	 * does nothing.
	 * 
	 * @param unit is the actual unit being put 
	 * on the specified position.
	 * @param x is the x position.
	 * @param y is the y position.
	 * @return true when the unit has been put on the 
	 * specified position.
	 */
	private synchronized boolean putUnit(Unit unit, int x, int y)
	{

		if (map[x][y] != null)
			return false;
        unit.setPosition(x, y);
		map[x][y] = unit;
        map[x][y].setAdjacent(adjacent(x,y));

		return true;
	}

	/**
	 * Get a unit from a position.
	 * 
	 * @param x position.
	 * @param y position.
	 * @return the unit at the specified position, or return
	 * null if there is no unit at that specific position.
	 */
	public Unit getUnit(int x, int y)
	{
		assert x >= 0 && x < map.length;
		assert y >= 0 && y < map[0].length;

		return map[x][y];
	}

	/**
	 * Move the specified unit a certain number of steps.
	 * 
	 * @param unit is the unit being moved.
	 * @param newX is the delta in the x position.
	 * @param newY is the delta in the y position.
	 * 
	 * @return true on success.
	 */
	private synchronized boolean moveUnit(Unit unit, int newX, int newY)
	{
		int originalX = unit.getX();
		int originalY = unit.getY();

		if (unit.getHitPoints() <= 0)
			return false;

		if (newX >= 0 && newX < BattleField.MAP_WIDTH)
			if (newY >= 0 && newY < BattleField.MAP_HEIGHT)
				if (map[newX][newY] == null) {
					if (putUnit(unit, newX, newY)) {
						map[originalX][originalY] = null;
                        map[newX][newY].setAdjacent(adjacent(newX,newY));
                        System.out.println(unit.getUnitID()+" move from "+"<"+originalX+","+originalY+">"+" to "+"<"+newX+","+newY+">");
						return true;
					}
				}

		return false;
	}

	/**
	 * Remove a unit from a specific position and makes the unit disconnect from the server.
	 * 
	 * @param x position.
	 * @param y position.
	 */
	private synchronized void removeUnit(int x, int y)
	{
		Unit unitToRemove = this.getUnit(x, y);
		if (unitToRemove == null)
			return; // There was no unit here to remove
		map[x][y] = null;
		units.remove(unitToRemove);
        for (int i=0; i<Lqueue.size();i++){
            if (((Unit)Lqueue.get(i).get("unit")).getUnitID()==unitToRemove.getUnitID()){
                Lqueue.remove(i); }
        }
	}

	/**
	 * Returns a new unique unit ID.
	 * @return int: a new unique unit ID.
	 */
	public synchronized int getNewUnitID() {
		return ++lastUnitID;
	}


    public void onMessageReceivedinit(Message msg){
        synchronized(Lqueue) {
            if (((Unit) msg.get("unit")).isAdjacent() == true) {
                Lqueue.add(msg);
                System.out.println(Lqueue.size());
                return;
            }
        }
        onMessageReceived(msg);
    }


    public void adjacentfilter(Message msg) {
        if (msg.getContent().containsKey("unit") && ((Unit)msg.get("unit")).getUnitID().contains("PLAYER")){
            if (((Unit) msg.get("unit")).isAdjacent() == true) {
                synchronized(Lqueue) {
                    System.out.println("-------------------------------add to the Lqueue -------------------------");
                    ((Unit) msg.get("unit")).setAdjacent(false);
                    Lqueue.addLast(msg);
                }
                System.out.println("R1 the Lqueue size is "+ Lqueue.size());

                return ;
            }
        }
    }

	public Message onMessageReceived(Message msg) {
        adjacentfilter(msg);
		Message reply = null;
        Message notification = null;
        NodeAddress origin = msg.getOrigin();
        NodeAddress target = null;
		MessageRequest request = (MessageRequest)msg.get("request");
		Unit unit;
		switch(request)
		{
			case spawnUnit:
                this.spawnUnit((Unit) msg.get("unit"), (Integer) msg.get("x"), (Integer) msg.get("y"));
                reply= messagefactory.createMessage();
                reply.put("request", MessageRequest.ADJinit);
                reply.put("adjacent", map[(Integer) msg.get("x")][(Integer) msg.get("y")].isAdjacent());
                break;
			case putUnit:
				this.putUnit((Unit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y"));
                reply=messagefactory.createMessage();

                reply.put("request", MessageRequest.ADJinit);
                reply.put("adjacent", map[(Integer) msg.get("x")][(Integer) msg.get("y")].isAdjacent());
				break;
			case getUnit:
			{
				reply = messagefactory.createMessage();
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");

				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
                reply.put("request",MessageRequest.reply);
				reply.put("id", msg.get("id"));
				// Get the unit at the specific location
				reply.put("unit", getUnit(x, y));

				break;
			}
			case getType:
			{
				reply = messagefactory.createMessage();
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");


				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
                reply.put("request",MessageRequest.reply);
				reply.put("id", msg.get("id"));
				if (getUnit(x, y) instanceof Player)
					reply.put("type", UnitType.player);
				else if (getUnit(x, y) instanceof Dragon)
					reply.put("type", UnitType.dragon);
				else reply.put("type", UnitType.undefined);
				break;
			}
			case dealDamage:
			{
                reply = messagefactory.createMessage();
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
                int ox = (Integer)msg.get("ox");
                int oy = (Integer)msg.get("oy");
                synchronized (map){
                    map[ox][oy].setAdjacent(adjacent(ox,oy));
                }
                reply.put("request",MessageRequest.reply);
                reply.put("id", msg.get("id"));
                reply.put("adjacent",map[ox][oy].isAdjacent());
                unit = this.getUnit(x, y);
				if (unit != null){
					unit.adjustHitPoints( -(Integer)msg.get("damage") );
                    notification = messagefactory.createMessage();
                    notification.put("request", MessageRequest.notification);
                    notification.put("damage", msg.get("damage"));
                    notification.put("from", origin);
                    target = unit.getNode().getAddress();
                }

				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */

				break;
			}
			case healDamage:
			{
                reply = messagefactory.createMessage();
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
                int ox = (Integer)msg.get("ox");
                int oy = (Integer)msg.get("oy");
				unit = this.getUnit(x, y);
                synchronized (map){
                    map[ox][oy].setAdjacent(adjacent(ox,oy));
                }
                reply.put("request",MessageRequest.reply);
                reply.put("id", msg.get("id"));
                reply.put("adjacent",map[ox][oy].isAdjacent());
				if (unit != null){
					unit.adjustHitPoints( (Integer)msg.get("healed") );
                    notification = messagefactory.createMessage();
                    notification.put("request",MessageRequest.notification);
                    notification.put("heal",unit.getHitPoints());
                    notification.put("from", origin);
                    target = unit.getNode().getAddress();
                }
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				break;
			}
			case moveUnit:{
				reply = messagefactory.createMessage();
				this.moveUnit((Unit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y"));
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
                int x = (Integer)msg.get("x");
                int y = (Integer)msg.get("y");
                int ox = (Integer)msg.get("ox");
                int oy = (Integer)msg.get("oy");
                reply.put("request", MessageRequest.reply);
				reply.put("id", msg.get("id"));
                reply.put("adjacent", map[x][y].isAdjacent());
				break;
            }
			case removeUnit:{
				this.removeUnit((Integer)msg.get("x"), (Integer)msg.get("y"));
				return null;
            }
            case testconnection:{
                for (Unit unit1 : units) {
                    if (unit1.getUnitID().equals(MessageRequest.testconnection)) {

                    }
                }
            }
		}

		try {
			if (reply != null) {
                serverSocket.sendMessage(reply, origin);
            }
            if (notification !=null){
                serverSocket.sendMessage(notification, target);
            }
		}
		catch(IDNotAssignedException idnae)  {
			// Could happen if the target already logged out
			idnae.printStackTrace();
		}
        return null;
	}





	/**
	 * Close down the battlefield. Unregisters
	 * the serverSocket so the program can 
	 * actually end.
	 */ 
	public synchronized void shutdown() {
		// Remove all units from the battlefield and make them disconnect from the server
		for (Unit unit : units) {
			unit.disconnect();
			unit.stopRunnerThread();
		}

		serverSocket.unRegister();
	}

    /*
     * customer function to detect the adjacent case
     */

    public boolean adjacent (int x ,int y ){
        boolean adjacent = false;
        if (x==0 && y==0) {
            adjacent = (map[x + 1][y] != null) || (map[x][y + 1] != null);
        }
        else if (x==0 && y< MAP_HEIGHT-1 && y >0) {
            adjacent = (map[x + 1][y] != null) || (map[x][y + 1] != null) || (map[x][y - 1] != null);
        }
        else if (x==0 && y== MAP_HEIGHT-1) {
            adjacent = (map[x + 1][y] != null) || (map[x][y - 1] != null);
        }
        else if (x>0 && x< MAP_WIDTH-1 && y==0) {
            adjacent = (map[x + 1][y] != null) || (map[x][y + 1] != null) || (map[x - 1][y] != null);
        }
        else if (x>0 && x< MAP_WIDTH-1 && y>0 && y< MAP_HEIGHT-1) {
            adjacent = (map[x + 1][y] != null) || (map[x][y + 1] != null) || (map[x - 1][y] != null) || (map[x][y - 1] != null);
        }
        else if (x>0 && x< MAP_WIDTH-1 && y== MAP_HEIGHT-1) {
            adjacent = (map[x + 1][y] != null) || (map[x][y - 1] != null) || (map[x - 1][y] != null) || (map[x][y - 1] != null);
        }
        else if (x== MAP_WIDTH-1 && y== MAP_HEIGHT-1) {
            adjacent = (map[x - 1][y] != null) || (map[x][y - 1] != null);
        }
        else if (x== MAP_WIDTH-1 && y==0) {
            adjacent = (map[x - 1][y] != null) || (map[x][y + 1] != null);
        }
        else if (x== MAP_WIDTH-1 && y>0 && y< MAP_HEIGHT-1) {
            adjacent = (map[x - 1][y] != null) || (map[x][y + 1] != null) || (map[x][y - 1] != null);
        }
        return adjacent ;
    }

    public void StartReleaseThread() {
        timer.schedule(new TimerTask(){
            public void run(){
                if(Lqueue.size()>10) {
                    LinkedList<Message> temp = (LinkedList<Message>) Lqueue.clone();
                    Lqueue.clear();
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    for(int i= 0; i< temp.size(); i++){
                                        Message releaseMessage = messagefactory.createMessage();
                                        NodeAddress target = ((Unit) temp.get(i).get("unit")).getNode().getAddress();
                                        releaseMessage.put("request",MessageRequest.release);
                                        releaseMessage.put("release",true);
                                        releaseMessage.put("x",((Unit)temp.get(i).get("unit")).getX());
                                        releaseMessage.put("y",((Unit)temp.get(i).get("unit")).getY());
                                        serverSocket.sendMessage(releaseMessage, target);
                                    }
                                }
                            }
                    ).start();
                }
            }},500,5000);
    }

    public void StartLexectueThread() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            synchronized(Lqueue) {
                                if (Lqueue.size()>0) {
                                    onMessageReceived(Lqueue.getFirst());
                                    Lqueue.removeFirst();
                                }
                            }

                        }
                    }
                }
        ).start();
    }

}
