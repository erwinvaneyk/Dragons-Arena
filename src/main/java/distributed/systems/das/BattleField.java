package distributed.systems.das;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

import distributed.systems.core.SynchronizedQueue;
import distributed.systems.das.units.Dragon;
import distributed.systems.das.units.Player;
import distributed.systems.das.units.Unit;
import distributed.systems.das.units.Unit.UnitType;
import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.Socket;
import distributed.systems.core.exception.IDNotAssignedException;
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
public class BattleField implements Serializable, IMessageReceivedHandler {
	/* The array of units */
	private Unit[][] map; // TODO: do it

	/* The static singleton */
	private transient static BattleField battlefield;

	/* Primary socket of the battlefield */
	@Setter
	private transient Socket serverSocket;
	
	/* The last id that was assigned to an unit. This variable is used to
	 * enforce that each unit has its own unique id.
	 */
	private int lastUnitID = 0; // TODO: do it

	public final static String serverID = "server";
	public final static int MAP_WIDTH = 25;
	public final static int MAP_HEIGHT = 25;
	private ArrayList<Unit> units;
    public SynchronizedQueue squeue;
    public LinkedList<Message> Lqueue;
    //public

	/**
	 * Initialize the battlefield to the specified size 
	 * @param width of the battlefield
	 * @param height of the battlefield
	 */
	private BattleField(int width, int height) {
		map = new Unit[width][height];
		units = new ArrayList<>();
        squeue = new SynchronizedQueue();
        Lqueue = new LinkedList<Message>();
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            synchronized(Lqueue) {
                                if (Lqueue.size()>0) {
                                    onMessageReceived2(Lqueue.poll());
                                }
                            }
                        }
                    }
                }
        ).start();
	}

	/**
	 * Singleton method which returns the sole 
	 * instance of the battlefield.
	 * 
	 * @return the battlefield.
	 */
	public static BattleField getBattleField() {
		if (battlefield == null)
			battlefield = new BattleField(MAP_WIDTH, MAP_HEIGHT);
		return battlefield;
	}

	public static void setBattlefield(BattleField battlefield) {
		BattleField.battlefield = battlefield;
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

		map[x][y] = unit;
		unit.setPosition(x, y);
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
		//unitToRemove.disconnect();
		units.remove(unitToRemove);
        for (int i=0; i<Lqueue.size();i++){
            if (((Unit)Lqueue.get(i).get("unit")).getUnitID()==unitToRemove.getUnitID()){
                Lqueue.remove(i);
            }
        }
	}

	/**
	 * Returns a new unique unit ID.
	 * @return int: a new unique unit ID.
	 */
	public synchronized int getNewUnitID() {
		return ++lastUnitID;
	}


	public void onMessageReceived(Message msg) {

        synchronized(Lqueue) {
            if (msg.getContent().containsKey("unit")){
                if (((Unit) msg.get("unit")).isAdjacent() == true) {
                    Lqueue.addLast(msg);
                    return;
                }
            }

        }
		Message reply = null;
        Message notifacation = null;
		String origin = (String)msg.get("origin");
        String target = new String();
		MessageRequest request = (MessageRequest)msg.get("request");
		Unit unit;
		switch(request)
		{
			case spawnUnit:
                this.spawnUnit((Unit) msg.get("unit"), (Integer) msg.get("x"), (Integer) msg.get("y"));
                reply=new Message();
                reply.put("adjacent", map[(Integer) msg.get("x")][(Integer) msg.get("y")].isAdjacent());
                break;
			case putUnit:
				this.putUnit((Unit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y"));
                reply=new Message();
                reply.put("adjacent", map[(Integer) msg.get("x")][(Integer) msg.get("y")].isAdjacent());
				break;
			case getUnit:
			{
				reply = new Message();
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
                int ox = ((Unit)msg.get("unit")).getX();
                int oy = ((Unit)msg.get("unit")).getY();
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				reply.put("id", msg.get("id"));
				// Get the unit at the specific location
				reply.put("unit", getUnit(x, y));
                map[ox][oy].setAdjacent(adjacent(ox,oy));
                reply.put("adjacent",map[ox][oy].isAdjacent());
				break;
			}
			case getType:
			{
				reply = new Message();
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
                int ox = ((Unit)msg.get("unit")).getX();
                int oy = ((Unit)msg.get("unit")).getY();
                reply.put("adjacent",map[ox][oy].isAdjacent());
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
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
                reply = new Message();

                int ox = ((Unit)msg.get("unit")).getX();
                int oy = ((Unit)msg.get("unit")).getY();
                reply.put("id", msg.get("id"));
                //System.out.println("the Battle Field deal Damage "+ msg.toString());
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				unit = this.getUnit(x, y);
                map[ox][oy].setAdjacent(adjacent(ox,oy));
                reply.put("adjacent",map[ox][oy].isAdjacent());
				if (unit != null){
					unit.adjustHitPoints( -(Integer)msg.get("damage") );
                    notifacation = new Message();
                    notifacation.put("damage",msg.get("damage"));
                    notifacation.put("from",origin);
                    target = "localsocket://"+unit.getUnitID();

                    if (unit.lived==false){
                        this.removeUnit(x, y);
                    }
                }

				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */

				break;
			}
			case healDamage:
			{
                reply = new Message();
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
                int ox = ((Unit)msg.get("unit")).getX();
                int oy = ((Unit)msg.get("unit")).getY();
				unit = this.getUnit(x, y);
                map[ox][oy].setAdjacent(adjacent(ox,oy));
                reply.put("adjacent",map[ox][oy].isAdjacent());
				if (unit != null){
					unit.adjustHitPoints( (Integer)msg.get("healed") );
                    target = "localsocket://"+unit.getUnitID();
                    notifacation = new Message();
                    notifacation.put("heal",unit.getHitPoints());
                    notifacation.put("from",origin);
                }
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				break;
			}
			case moveUnit:
				reply = new Message();
				this.moveUnit((Unit) msg.get("unit"), (Integer) msg.get("x"), (Integer) msg.get("y"));
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
                int ox = ((Unit)msg.get("unit")).getX();
                int oy = ((Unit)msg.get("unit")).getY();
				reply.put("id", msg.get("id"));
                reply.put("adjacent", map[ox][oy].isAdjacent());
				break;
			case removeUnit:
				this.removeUnit((Integer)msg.get("x"), (Integer)msg.get("y"));
				return;
		}

		try {
			if (reply != null){
				serverSocket.sendMessage(reply, origin);
            }
            if (notifacation !=null){
                System.out.println("notifaction is " + notifacation.toString());
                serverSocket.sendMessage(notifacation, target);
            }

		}
		catch(IDNotAssignedException idnae)  {
			// Could happen if the target already logged out
			idnae.printStackTrace();
		}
	}

    public void onMessageReceived2(Message msg) {

        Message reply = null;
        Message notifacation = null;
        String origin = (String)msg.get("origin");
        String target = new String();
        MessageRequest request = (MessageRequest)msg.get("request");
        Unit unit;
        switch(request)
        {
            case spawnUnit:
                this.spawnUnit((Unit) msg.get("unit"), (Integer) msg.get("x"), (Integer) msg.get("y"));
                reply=new Message();
                reply.put("adjacent", map[(Integer) msg.get("x")][(Integer) msg.get("y")].isAdjacent());
                break;
            case putUnit:
                this.putUnit((Unit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y"));
                reply=new Message();
                reply.put("adjacent", map[(Integer) msg.get("x")][(Integer) msg.get("y")].isAdjacent());
                break;
            case getUnit:
            {
                reply = new Message();
                int x = (Integer)msg.get("x");
                int y = (Integer)msg.get("y");
                int ox = ((Unit)msg.get("unit")).getX();
                int oy = ((Unit)msg.get("unit")).getY();
				/* Copy the id of the message so that the unit knows
				 * what message the battlefield responded to.
				 */
                reply.put("id", msg.get("id"));
                // Get the unit at the specific location
                reply.put("unit", getUnit(x, y));
                reply.put("adjacent",map[ox][oy].isAdjacent());
                break;
            }
            case getType:
            {
                reply = new Message();
                int x = (Integer)msg.get("x");
                int y = (Integer)msg.get("y");

				/* Copy the id of the message so that the unit knows
				 * what message the battlefield responded to.
				 */

                if (getUnit(x, y) instanceof Player)
                    reply.put("type", UnitType.player);
                else if (getUnit(x, y) instanceof Dragon)
                    reply.put("type", UnitType.dragon);
                else reply.put("type", UnitType.undefined);

                reply.put("id", msg.get("id"));
                break;
            }
            case dealDamage:
            {
                reply = new Message();

                int ox = ((Unit)msg.get("unit")).getX();
                int oy = ((Unit)msg.get("unit")).getY();
                reply.put("id", msg.get("id"));
                int x = (Integer)msg.get("x");
                int y = (Integer)msg.get("y");
                unit = this.getUnit(x, y);
                if (unit != null){
                    unit.adjustHitPoints( -(Integer)msg.get("damage") );
                    notifacation = new Message();
                    target = "localsocket://"+unit.getUnitID();
                    notifacation.put("damage",msg.get("damage"));
                    if (unit.lived==false){
                        this.removeUnit(x, y);
                    }
                }
                System.out.println("location is "+"<"+ox+","+oy+">");
                map[ox][oy].setAdjacent(adjacent(ox,oy));
                reply.put("adjacent",map[ox][oy].isAdjacent());
				/* Copy the id of the message so that the unit knows
				 * what message the battlefield responded to.
				 */
                break;
            }
            case healDamage:
            {
                reply = new Message();

                int x = (Integer)msg.get("x");
                int y = (Integer)msg.get("y");
                int ox = ((Unit)msg.get("unit")).getX();
                int oy = ((Unit)msg.get("unit")).getY();
                unit = this.getUnit(x, y);
                map[ox][oy].setAdjacent(adjacent(ox,oy));
                reply.put("adjacent",map[ox][oy].isAdjacent());
                if (unit != null){
                    unit.adjustHitPoints( (Integer)msg.get("healed") );
                    target = "localsocket://"+unit.getUnitID();
                    notifacation = new Message();
                    notifacation.put("heal",unit.getHitPoints());
                }
				/* Copy the id of the message so that the unit knows
				 * what message the battlefield responded to.
				 */
                break;
            }
            case moveUnit:
                reply = new Message();
                this.moveUnit((Unit) msg.get("unit"), (Integer) msg.get("x"), (Integer) msg.get("y"));
				/* Copy the id of the message so that the unit knows
				 * what message the battlefield responded to.
				 */
                int ox = ((Unit)msg.get("unit")).getX();
                int oy = ((Unit)msg.get("unit")).getY();
                reply.put("id", msg.get("id"));
                reply.put("adjacent",map[ox][oy].isAdjacent());
                break;
            case removeUnit:
                this.removeUnit((Integer)msg.get("x"), (Integer)msg.get("y"));
                return;
        }

        try {
            if (reply != null){

                serverSocket.sendMessage(reply, origin);
            }
            if (notifacation !=null){
                System.out.println("target is "+target);
                serverSocket.sendMessage(notifacation, target);
            }

        }
        catch(IDNotAssignedException idnae)  {
            // Could happen if the target already logged out
            idnae.printStackTrace();
        }
    }

    public boolean adjacent (int x ,int y ){
        boolean adjacent = false;
        if (x==0 && y==0) {
            if ((map[x+1][y]!=null)|| (map[x][y+1]!=null)){
                adjacent =  true;
            }
            else {adjacent = false;}
        }
        else if (x==0 && y<this.MAP_HEIGHT-1 && y >0) {
            if ((map[x+1][y]!=null)||(map[x][y+1]!=null)||(map[x][y-1]!=null)){
                adjacent = true;
            }
            else {adjacent = false;}
        }
        else if (x==0 && y==this.MAP_HEIGHT-1) {
            if ((map[x+1][y]!=null)||(map[x][y-1]!=null)){
                adjacent = true;
            }
            else {adjacent = false;}
        }
        else if (x>0 && x<this.MAP_WIDTH-1 && y==0) {
            if ((map[x+1][y]!=null)||(map[x][y+1]!=null)||(map[x-1][y]!=null)){
                adjacent = true;
            }
            else {adjacent = false;}
        }
        else if (x>0 && x<this.MAP_WIDTH-1 && y>0 && y<this.MAP_HEIGHT-1) {
            if ((map[x+1][y]!=null)||(map[x][y+1]!=null)||(map[x-1][y]!=null)||(map[x][y-1]!=null)){
                adjacent = true;
            }
            else {adjacent = false;}
        }
        else if (x>0 && x<this.MAP_WIDTH-1 && y==this.MAP_HEIGHT-1) {
            if ((map[x+1][y]!=null)||(map[x][y-1]!=null)||(map[x-1][y]!=null)||(map[x][y-1]!=null)){
                adjacent = true;
            }
            else {adjacent = false;}
        }
        else if (x==this.MAP_WIDTH-1 && y==this.MAP_HEIGHT-1) {
            if ((map[x-1][y]!=null)|| (map[x][y-1]!=null)){
                adjacent = true;
            }
            else {adjacent = false;}
        }
        else if (x==this.MAP_WIDTH-1 && y==0) {
            if ((map[x-1][y]!=null)||(map[x][y+1]!=null)){
                adjacent = true;
            }
            else {adjacent = false;}
        }
        else if (x==this.MAP_WIDTH-1 && y>0 && y<this.MAP_HEIGHT-1) {
            if ((map[x-1][y]!=null)||(map[x][y+1]!=null)||(map[x][y-1]!=null)){
                adjacent = true;
            }
            else {adjacent = false;}
        }
        return adjacent ;
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
	
}
