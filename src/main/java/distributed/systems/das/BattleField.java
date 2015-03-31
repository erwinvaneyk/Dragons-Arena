package distributed.systems.das;


import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;
import distributed.systems.core.ExtendedSocket;
import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.core.exception.IDNotAssignedException;
import distributed.systems.das.units.Dragon;
import distributed.systems.das.units.Player;
import distributed.systems.das.units.Unit;
import distributed.systems.das.units.Unit.UnitType;
import distributed.systems.network.NodeAddress;
import distributed.systems.network.ServerSocket;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

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

	// Communicated over messages:
	private ArrayList<Unit> units;
	private int lastUnitID = 0;
	private Unit[][] map;

	@Setter
	private transient MessageFactory messagefactory;
	/* Primary socket of the battlefield */
	@Setter
	private transient ServerSocket serverSocket;

	public final static String serverID = "server";
	public final static int MAP_WIDTH = 25;
	public final static int MAP_HEIGHT = 25;
    private transient Timer timer=new Timer();
	/**
	 * Initialize the battlefield to the specified size 
	 * @param width of the battlefield
	 * @param height of the battlefield
	 */	private BattleField(int width, int height) {
        map = new Unit[width][height];
        units = new ArrayList<>();
    }

	public BattleField() {
		map = new Unit[MAP_WIDTH][MAP_HEIGHT];
		units = new ArrayList<>();
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
	public Optional<Unit> getUnit(int x, int y)
	{
		assert x >= 0 && x < map.length;
		assert y >= 0 && y < map[0].length;
		return Optional.ofNullable(map[x][y]);
	}

	public Optional<Unit> getNearest(UnitType type, int originX, int originY) {
		List<Unit> dragons = units.stream().filter(t -> type == null || t.getType() == type).collect(toList());
		if(dragons.size() == 1) {
			return Optional.of(dragons.get(0));
		}
		return dragons.stream().reduce((a, b) -> {
			int valueA = Math.abs(originX - a.getX()) + Math.abs(originY - a.getY());
			int valueB = Math.abs(originX - b.getX()) + Math.abs(originY - b.getY());
			return valueA - valueB > 0 ? b : a;
		});
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
		Optional<Unit> unitToRemove = this.getUnit(x, y);
		unitToRemove.ifPresent(unit -> {
			map[x][y] = null;
			units.remove(unit);
		});
	}

	/**
	 * Returns a new unique unit ID.
	 * @return int: a new unique unit ID.
	 */
	public synchronized int getNewUnitID() {
		return ++lastUnitID;
	}

	public Message onMessageReceived(Message msg) {
		Message reply = null;
        final Message notification = messagefactory.createMessage();
        NodeAddress origin = msg.getOrigin();
		MessageRequest request = (MessageRequest)msg.get("request");
		Unit tempunit;
        final int[] currenthp = new int[1];
        boolean result;
        Message updatemessage = null;
		switch(request)
		{
			case spawnUnit:
                result = this.spawnUnit((Unit) msg.get("unit"), (Integer) msg.get("x"), (Integer) msg.get("y"));
                reply= messagefactory.createMessage();
                reply.put("request", MessageRequest.ADJinit);
                reply.put("adjacent", map[(Integer) msg.get("x")][(Integer) msg.get("y")].isAdjacent());
                if (result ==true){
                    int x = (Integer)msg.get("x");
                    int y = (Integer)msg.get("y");
                    updatemessage=messagefactory.createMessage();
                    updatemessage.put("request", MessageRequest.update);
                    updatemessage.put("type", MessageRequest.spawnUnit);
                    updatemessage.put("x", msg.get("x"));
                    updatemessage.put("y", msg.get("y"));
                    updatemessage.put("unit",(Unit)msg.get("unit"));
                }
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
				reply.put("unit", getUnit(x, y).orElse(null));

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
				reply.put("type", getUnit(x, y)
						.map(Unit::getType)
						.orElse(UnitType.undefined));
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
				Optional<Unit> optUnit = this.getUnit(x, y);
				optUnit.ifPresent(unit -> {
                    currenthp[0] = unit.adjustHitPoints(-(Integer) msg.get("damage"));
                    notification.put("request", MessageRequest.notification);
                    notification.put("damage", msg.get("damage"));
                    notification.put("from", origin);
					serverSocket.sendMessage(notification, unit.getAddress());
                });
                updatemessage = messagefactory.createMessage();
                updatemessage.put("request", MessageRequest.update);
                updatemessage.put("type", MessageRequest.dealDamage);
                updatemessage.put("x", msg.get("x"));
                updatemessage.put("y", msg.get("y"));
                updatemessage.put("hp", currenthp[0]);
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
				Optional<Unit> optUnit = this.getUnit(x, y);
                synchronized (map){
                    map[ox][oy].setAdjacent(adjacent(ox,oy));
                }
                reply.put("request",MessageRequest.reply);
                reply.put("id", msg.get("id"));
                reply.put("adjacent",map[ox][oy].isAdjacent());
				optUnit.ifPresent(unit -> {
                    unit.adjustHitPoints( (Integer)msg.get("healed") );
                    notification.put("request",MessageRequest.notification);
                    notification.put("heal",unit.getHitPoints());
                    notification.put("from", origin);
					serverSocket.sendMessage(notification, unit.getAddress());
				});
                updatemessage = messagefactory.createMessage();
                updatemessage.put("request", MessageRequest.update);
                updatemessage.put("type", MessageRequest.healDamage);
                updatemessage.put("x", x);
                updatemessage.put("y", y);
                updatemessage.put("hp", this.getUnit(x,y).get().getHitPoints());
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				break;
			}
			case moveUnit:{
				reply = messagefactory.createMessage();
				result = this.moveUnit((Unit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y"));
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
                if (result){
                    updatemessage = messagefactory.createMessage();
                    updatemessage.put("request", MessageRequest.update);
                    updatemessage.put("type", MessageRequest.moveUnit);
                    updatemessage.put("ox", ox);
                    updatemessage.put("oy", oy);
                    updatemessage.put("tx", x);
                    updatemessage.put("ty", y);
                }

				break;
            }
			case removeUnit:{
                int x = (Integer)msg.get("x");
                int y = (Integer)msg.get("y");
				this.removeUnit((Integer)msg.get("x"), (Integer)msg.get("y"));
                updatemessage = messagefactory.createMessage();
                updatemessage.put("request", MessageRequest.update);
                updatemessage.put("type", MessageRequest.removeUnit);
                updatemessage.put("x", x);
                updatemessage.put("y", y);
				return updatemessage;
            }
            case update:{
                MessageRequest updatetype = (MessageRequest)msg.get("type");
                switch(updatetype){
                    case spawnUnit:{
                        this.spawnUnit((Unit) msg.get("unit"), (Integer) msg.get("x"), (Integer) msg.get("y"));
                        if(this.getUnit((Integer) msg.get("x"),(Integer) msg.get("y"))!=null){
                        }
                        break;

                    }
                    case healDamage:{
                        int x = (Integer)msg.get("x");
                        int y = (Integer)msg.get("y");
                        Optional<Unit> optUnit = this.getUnit(x, y);
                        optUnit.ifPresent(unit -> unit.setHitPoints((Integer) msg.get("hp")));
                        break;
                    }
                    case dealDamage:{
                        int x = (Integer)msg.get("x");
                        int y = (Integer)msg.get("y");
                        Optional<Unit> optUnit = this.getUnit(x, y);
                        optUnit.ifPresent(unit -> unit.setHitPoints((Integer)msg.get("hp")));
                        break;
                    }
                    case moveUnit:{
                        int tx = (Integer)msg.get("tx");
                        int ty = (Integer)msg.get("ty");
                        int ox = (Integer)msg.get("ox");
                        int oy = (Integer)msg.get("oy");
	                    this.getUnit(ox, oy).ifPresent(unit -> {
		                    this.moveUnit(unit, tx, ty);
	                    });
                        break;
                    }
                    case removeUnit:{
                        System.out.println("In Battle Field ,I got a removeUnit Update message ");
                        int x = (Integer)msg.get("x");
                        int y = (Integer)msg.get("y");
                        this.removeUnit(x, y);
                        break;
                    }

                }
	            break;
            }
			case getAdjacent: {
				reply = messagefactory.createMessage();
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				reply.put("id", msg.get("id"));
				reply.put("request", MessageRequest.reply);
				ArrayList<Unit> adjacentUnits = new ArrayList<>();
				if(x - 1 > 0) getUnit(x - 1, y).ifPresent(adjacentUnits::add);
				if(x + 1 < BattleField.MAP_WIDTH) getUnit(x + 1, y).ifPresent(adjacentUnits::add);
				if(y - 1 > 0) getUnit(x, y - 1).ifPresent(adjacentUnits::add);
				if(y + 1 < BattleField.MAP_HEIGHT) getUnit(x, y + 1).ifPresent(adjacentUnits::add);
				reply.put("adjacentUnits", adjacentUnits);
				break;
			}
			case getNearest: {
				reply = messagefactory.createMessage();
				UnitType unit = (UnitType) msg.get("type");
				reply.put("id", msg.get("id"));
				reply.put("request", MessageRequest.reply);
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				reply.put("unit", getNearest(unit, x, y).orElse(null));
				break;
			}
		}

		try {
			if (reply != null) {
				serverSocket.sendMessage(reply, origin);
            }
		}
		catch(IDNotAssignedException idnae)  {
			// Could happen if the target already logged out
			idnae.printStackTrace();
		}
        return updatemessage;
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

		//serverSocket.unRegister();
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
}
