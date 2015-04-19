package distributed.systems.das.units;


import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.core.MessageFactory;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.core.exception.IDNotAssignedException;
import distributed.systems.das.GameState;
import distributed.systems.das.MessageRequest;
import distributed.systems.network.ClientNode;
import distributed.systems.network.PlayerState;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base class for all players whom can 
 * participate in the DAS game. All properties
 * of the units (hitpoints, attackpoints) are
 * initialized in this class.
 *  
 * @author Pieter Anemaet, Boaz Pat-El
 */
@ToString
public abstract class Unit implements Serializable, IMessageReceivedHandler {
	private static final long serialVersionUID = -4550572524008491161L;

	protected transient final ClientNode node;
	private transient final MessageFactory messageFactory;

	@Getter
	private PlayerState playerState;

	// Position of the unit
	protected int x, y;

	// Health
	private int maxHitPoints;
	protected int hitPoints;

	// Attack points
	protected int attackPoints;

	// Identifier of the unit
	@Setter
	private String unitID;
	
	// Map messages from their ids
	private Map<Integer, Message> messageList;
	// Is used for mapping an unique id to a message sent by this unit
	private int localMessageCounter = 0;

	// If this is set to false, the unit will return its run()-method and disconnect from the server
	protected boolean running;

	/* The thread that is used to make the unit run in a separate thread.
	 * We need to remember this thread to make sure that Java exits cleanly.
	 * (See stopRunnerThread())
	 */
	protected transient Thread runnerThread;

	public enum Direction {
		up, right, down, left
	}

	public enum UnitType {
		player, dragon, undefined,
	}
    /*
     * some field need to be consist
     */

    @Getter@Setter
    public boolean lived;
    public static final int ADJ_UP =1;
    public static final int ADJ_RIGHT =2;
    public static final int ADJ_DOWN =3;
    public static final int ADJ_LEFT =4;
    public static final int ADJ_NONE =0;
    @Getter@Setter
    public int disconnect;
    @Getter@Setter
    private boolean adjacent;

	/**
	 * Create a new unit and specify the
	 * number of hitpoints. Units hitpoints
	 * are initialized to the maxHitPoints.
	 *
	 * @param maxHealth is the maximum health of
	 * this specific unit.
	 */
	public Unit(int maxHealth, int attackPoints, ClientNode node) throws AlreadyAssignedIDException {
		messageList = new HashMap<>();

		// Initialize the max health and health
		hitPoints = maxHitPoints = maxHealth;

		// Initialize the attack points
		this.attackPoints = attackPoints;

		// Get a new unit id
		this.unitID = node.getAddress().getName();
		this.node = node;
		this.messageFactory = this.node.getMessageFactory();
		this.playerState = node.getPlayerState();
        this.lived = true;

	}

	/**
	 * Adjust the hitpoints to a certain level. 
	 * Useful for healing or dying purposes.
	 *
	 * @param modifier is to be added to the
	 * hitpoint count.
	 */
	public synchronized int adjustHitPoints(int modifier) {
		if (hitPoints <= 0)
			return 0;

		hitPoints = Math.min(hitPoints + modifier, maxHitPoints);

		if (hitPoints <= 0){
            hitPoints =0;
            this.lived = false;
			//removeUnit(x, y);
        }
        return this.hitPoints;

	}
	
	public void dealDamage(int x, int y, int damage) {
		/* Create a new message, notifying the board
		 * that a unit has been dealt damage.
		 */
		int id;
		Message damageMessage;
		synchronized (this) {
			id = localMessageCounter++;
			damageMessage = messageFactory.createMessage();
			damageMessage.put("request", MessageRequest.dealDamage);
			damageMessage.put("x", x);
			damageMessage.put("y", y);
            damageMessage.put("ox",this.getX());
            damageMessage.put("oy",this.getY());
			damageMessage.put("damage", damage);
			damageMessage.put("id", id);
            damageMessage.put("update",true);

		}
		
		// Send a spawn message
        if(this.lived){
	        node.sendMessageToServer(damageMessage);
        }
	}
	
	public void healDamage(int x, int y, int healed) {
		/* Create a new message, notifying the board
		 * that a unit has been healed.
		 */
		int id;
		Message healMessage;
		synchronized (this) {
			id = localMessageCounter++;
			healMessage =  messageFactory.createMessage();
			healMessage.put("request", MessageRequest.healDamage);
			healMessage.put("x", x);
			healMessage.put("y", y);
            healMessage.put("ox",this.getX());
            healMessage.put("oy",this.getY());
			healMessage.put("healed", healed);
			healMessage.put("id", id);
            healMessage.put("update", true);

		}

		// Send a spawn message

        if (this.lived) {
	        node.sendMessageToServer(healMessage);
        }
	}

	/**
	 * @return the maximum number of hitpoints.
	 */
	public int getMaxHitPoints() {
		return maxHitPoints;		
	}

	/**
	 * @return the unique unit identifier.
	 */
	public String getUnitID() {
		return unitID;
	}

	/**
	 * Set the position of the unit.
	 * @param x is the new x coordinate
	 * @param y is the new y coordinate
	 */
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the x position
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y position
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the current number of hitpoints.
	 */
	public int getHitPoints() {
		return hitPoints;
	}

    public void setHitPoints( int hitPoints) {
        this.hitPoints = hitPoints;
    }

	/**
	 * @return the attack points
	 */
	public int getAttackPoints() {
		return attackPoints;
	}

	/**
	 * Tries to make the unit spawn at a certain location on the battlefield
	 * @param x x-coordinate of the spawn location
	 * @param y y-coordinate of the spawn location
	 * @return true iff the unit could spawn at the location on the battlefield
	 */
	protected boolean spawn(int x, int y) {
		/* Create a new message, notifying the board
		 * the unit has actually spawned at the
		 * designated position. 
		 */
		int id = localMessageCounter++;
		Message spawnMessage =  messageFactory.createMessage();
		spawnMessage.put("request", MessageRequest.spawnUnit);
		spawnMessage.put("x", x);
		spawnMessage.put("y", y);
		spawnMessage.put("unit", this);
		spawnMessage.put("id", id);
        spawnMessage.put("update", true);

		// Send a spawn message
		try {
			node.sendMessageToServer(spawnMessage);
		} catch (IDNotAssignedException e) {
			System.err.println("No server found while spawning unit at location (" + x + ", " + y + ")");
			return false;
		}

		// Wait for the unit to be placed
		getUnit(x, y);

		return true;
	}
	
	/**
	 * Returns whether the indicated square contains a player, a dragon or nothing. 
	 * @param x: x coordinate
	 * @param y: y coordinate
	 * @return UnitType: the indicated square contains a player, a dragon or nothing.
	 */
	protected UnitType getType(int x, int y) {
		Message getMessage =  messageFactory.createMessage(), result;
		int id = localMessageCounter++;
		getMessage.put("request", MessageRequest.getType);
		getMessage.put("x", x);
		getMessage.put("y", y);
        getMessage.put("ox",this.getX());
        getMessage.put("oy",this.getY());
		getMessage.put("id", id);

		// Send the getUnit message
        if (this.lived) {
	        node.sendMessageToServer(getMessage);
        }
		// Wait for the reply
		while(!messageList.containsKey(id)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return UnitType.undefined;
		}

		result = messageList.get(id);
		if (result == null) // Could happen if the game window had closed
			return UnitType.undefined;
		messageList.put(id, null);

        if (result.getContent().containsKey("adjacent")){
            this.setAdjacent((Boolean) result.get("adjacent"));
        }

		return (UnitType) result.get("type");
	}

	protected Optional<Unit> getUnit(int x, int y)
	{
		Message getMessage =  messageFactory.createMessage(), result;
		int id = localMessageCounter++;
		getMessage.put("request", MessageRequest.getUnit);
		getMessage.put("x", x);
		getMessage.put("y", y);
        getMessage.put("ox",this.getX());
        getMessage.put("oy",this.getY());
		getMessage.put("id", id);

        // Send the getUnit message
        if (this.lived) {
	        node.sendMessageToServer(getMessage);
        }

		// Wait for the reply
		while(!messageList.containsKey(id)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return null;
		}

		result = messageList.get(id);
		messageList.put(id, null);


		return Optional.ofNullable((Unit) result.get("unit"));
	}

	protected List<Unit> getAdjacentUnits() {
		Message getMessage =  messageFactory.createMessage(), result;
		int id = localMessageCounter++;
		getMessage.put("request", MessageRequest.getAdjacent);
		getMessage.put("x", x);
		getMessage.put("y", y);
		getMessage.put("id", id);

		// Send the getUnit message
		if (this.lived) {
			node.sendMessageToServer(getMessage);
		}

		// Wait for the reply
		while(!messageList.containsKey(id)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return null;
		}

		result = messageList.get(id);
		messageList.put(id, null);


		return (List<Unit>) result.get("adjacentUnits");
	}

	protected Optional<Unit> getNearest(UnitType type) {
		Message getMessage =  messageFactory.createMessage(), result;
		int id = localMessageCounter++;
		getMessage.put("request", MessageRequest.getNearest);
		getMessage.put("type", type);
		getMessage.put("x", x);
		getMessage.put("y", y);
		getMessage.put("id", id);

		// Send the getUnit message
		if (this.lived) {
			node.sendMessageToServer(getMessage);
		}

		// Wait for the reply
		while(!messageList.containsKey(id)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return null;
		}

		result = messageList.get(id);
		messageList.put(id, null);

		return Optional.ofNullable((Unit) result.get("unit"));
	}

	protected void removeUnit(int x, int y)
	{
		Message removeMessage =  messageFactory.createMessage();
		int id = localMessageCounter++;
		removeMessage.put("request", MessageRequest.removeUnit);
		removeMessage.put("x", x);
		removeMessage.put("y", y);
		removeMessage.put("id", id);
        removeMessage.put("update",true);


		// Send the removeUnit message
		node.sendMessageToServer(removeMessage);
	}
	protected void moveUnit(int x, int y)
	{
		Message moveMessage =  messageFactory.createMessage(), result;
		int id = localMessageCounter++;
		moveMessage.put("request", MessageRequest.moveUnit);
		moveMessage.put("x", x);
		moveMessage.put("y", y);
        moveMessage.put("ox",this.getX());
        moveMessage.put("oy",this.getY());
		moveMessage.put("id", id);
		moveMessage.put("unit", this);
        moveMessage.put("update",true);

		// Send the getUnit message
        if (this.lived) {
	        node.sendMessageToServer(moveMessage);
        }

		// Wait for the reply
		while(!messageList.containsKey(id))
		{
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return;
		}

        /**
         * update the location
         * @author MA
         */
        result = messageList.get(id);
        this.x =x;
        this.y = y;
		// Remove the result from the messageList
		messageList.put(id, null);
	}

	public Message onMessageReceived(Message message) {
        if (message.getContent().containsKey("release")){
            this.setAdjacent(false);
            this.x= (int) message.get("x");
            this.x= (int) message.get("y");
        }
        if (message.getContent().containsKey("adjacent")){
            this.setAdjacent((Boolean) message.get("adjacent"));
        }

        if (message.getContent().containsKey("damage")) {
            this.adjustHitPoints(-(Integer) message.get("damage"));
            if (!this.lived){
                this.removeUnit(this.getX(),this.getY());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.disconnect();
            }
        }
        if (message.getContent().containsKey("healed")){
            if(this.lived){
                this.adjustHitPoints((Integer) message.get("healed"));
            }

        }
        if (message.getContent().containsKey("id")){
            messageList.put((Integer)message.get("id"), message);
        }
//		messageList.put((Integer)message.get("id"), message);
        return null;
	}
	
	// Disconnects the unit from the battlefield by exiting its run-state
	public void disconnect() {
		running = false;
		// #Hack for clientsockets not unregister-ing
		stopRunnerThread();
	}

	/**
	 * Stop the running thread. This has to be called explicitly to make sure the program 
	 * terminates cleanly.
	 */
	public void stopRunnerThread() {
		try {
			if(runnerThread != null) {
				runnerThread.join();
			}
		} catch (InterruptedException ex) {
			assert(false) : "Unit stopRunnerThread was interrupted";
		}
		
	}

	protected abstract void doAction();

	public UnitType getType() {
		return UnitType.undefined;
	}

	public int distanceTo(int x,int y) {
		return Math.abs(this.x - x) + Math.abs(this.y - y);
	}
}
