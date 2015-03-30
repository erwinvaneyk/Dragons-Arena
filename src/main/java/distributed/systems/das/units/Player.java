package distributed.systems.das.units;

import java.io.Serializable;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.BattleField;
import distributed.systems.das.GameState;
import distributed.systems.network.ClientNode;
import distributed.systems.network.logging.InfluxLogger;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A Player is, as the name implies, a playing 
 * character. It can move in the four wind directions,
 * has a hitpoint range between 10 and 20 
 * and an attack range between 1 and 10.
 * 
 * Every player runs in its own thread, simulating
 * individual behaviour, not unlike a distributed
 * server setup.
 *   
 * @author Pieter Anemaet, Boaz Pat-El
 */
@ToString(callSuper = true)
@SuppressWarnings("serial")
public abstract class Player extends Unit implements Runnable {
	/* Reaction speed of the player
	 * This is the time needed for the player to take its next turn.
	 * Measured in half a seconds x GAME_SPEED.
	 */
	public int timeBetweenTurns;
	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;
	public static final int MIN_HITPOINTS = 20;
	public static final int MAX_HITPOINTS = 10;
	public static final int MIN_ATTACKPOINTS = 1;
	public static final int MAX_ATTACKPOINTS = 10;

	/**
	 * Create a player, initialize both 
	 * the hit and the attackpoints. 
	 * @throws AlreadyAssignedIDException 
	 */
	public Player(int x, int y, ClientNode node) throws AlreadyAssignedIDException {
		/* Initialize the hitpoints and attackpoints */
		super((int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS), (int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS), node);

		/* Create a random delay */
		timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;

		/**
		 * update the local position
		 */
		this.x = x;
		this.y = y;
	}

	public void start() {

		if (!spawn(x, y)){
			return;
        }// We could not spawn on the battlefield

		/* Create a new player thread */
		//new Thread(this).start();
        this.setDisconnect(0);
		runnerThread = new Thread(this);
		runnerThread.start();
	}

	/**
	 * Roleplay the player. Make the player act once in a while,
	 * only stopping when the player is actually dead or the 
	 * program has halted.
	 * 
	 * It checks a random direction, if an entity is located there.
	 * If there is a player, it will try to heal that player if the
	 * 50% health rule applies. If there is a dragon, it will attack
	 * and if there is nothing, it will move in that direction. 
	 */
	@SuppressWarnings("static-access")
	public void run() {
		
		this.running = true;

		while(GameState.getRunningState() && this.running) {
			try {
				/* Sleep while the player is considering its next move */
				Thread.sleep((int) (timeBetweenTurns * 500 * GameState.GAME_SPEED));

				/* Stop if the player runs out of hitpoints */
				if (getHitPoints() <= 0)
					return;

				long start = System.currentTimeMillis();
				doAction();
				InfluxLogger.getInstance().logUnitRoundDuration(this, this.node.getServerAddress(), System.currentTimeMillis() - start);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//clientSocket.unRegister();
	}
}
