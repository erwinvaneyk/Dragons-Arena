package distributed.systems.das.units;

import java.io.Serializable;
import java.util.ArrayList;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.BattleField;
import distributed.systems.das.GameState;
import distributed.systems.network.ClientNode;
import distributed.systems.network.logging.InfluxLogger;
import lombok.ToString;

/**
 * A dragon is a non-playing character, which can't
 * move, has a hitpoint range between 50 and 100
 * and an attack range between 5 and 20.
 * 
 * Every dragon runs in its own thread, simulating
 * individual behaviour, not unlike a distributed
 * server setup.
 *   
 * @author Pieter Anemaet, Boaz Pat-El
 */
@ToString(callSuper = true)
@SuppressWarnings("serial")
public class Dragon extends Unit implements Runnable, Serializable {
	/* Reaction speed of the dragon
	 * This is the time needed for the dragon to take its next turn.
	 * Measured in half a seconds x GAME_SPEED.
	 */
	protected int timeBetweenTurns; 
	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;
	// The minimum and maximum amount of hitpoints that a particular dragon starts with
	public static final int MIN_HITPOINTS = 50;
	public static final int MAX_HITPOINTS = 100;
	// The minimum and maximum amount of hitpoints that a particular dragon has
	public static final int MIN_ATTACKPOINTS = 5;
	public static final int MAX_ATTACKPOINTS = 20;
	
	/**
	 * Spawn a new dragon, initialize the 
	 * reaction speed 
	 * @throws AlreadyAssignedIDException 
	 *
	 */
	public Dragon(int x, int y, ClientNode node) throws AlreadyAssignedIDException {
		/* Spawn the dragon with a random number of hitpoints between
		 * 50..100 and 5..20 attackpoints. */
		super((int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS)
				, (int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS)
				, node);

		/* Create a random delay */
		timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;

		this.x = x;
		this.y = y;
	}

	public void start() {
		if (!spawn(x, y))
			return; // We could not spawn on the battlefield

		/* Awaken the dragon */
		//new Thread(this).start();
		runnerThread = new Thread(this);
		runnerThread.start();
	}

	/**
	 * Roleplay the dragon. Make the dragon act once a while,
	 * only stopping when the dragon is actually dead or the 
	 * program has halted.
	 * 
	 * It checks if an enemy is near and, if so, it attacks that
	 * specific enemy.
	 */
	@SuppressWarnings("static-access")
	public void run() {
		ArrayList <Direction> adjacentPlayers = new ArrayList<>();
		
		this.running = true;

		while(GameState.getRunningState() && this.running) {
			long start = System.currentTimeMillis();
			try {
				/* Sleep while the dragon is considering its next move */
				Thread.currentThread().sleep((int)(timeBetweenTurns * 500 * GameState.GAME_SPEED));

				/* Stop if the dragon runs out of hitpoints */
				if (getHitPoints() <= 0)
					break;

				// Decide what players are near
				if (getY() > 0)
					if ( getType( getX(), getY() - 1 ) == UnitType.player )
						adjacentPlayers.add(Direction.up);
				if (getY() < BattleField.MAP_WIDTH - 1)
					if ( getType( getX(), getY() + 1 ) == UnitType.player )
						adjacentPlayers.add(Direction.down);
				if (getX() > 0)
					if ( getType( getX() - 1, getY() ) == UnitType.player )
						adjacentPlayers.add(Direction.left);
				if (getX() < BattleField.MAP_WIDTH - 1)
					if ( getType( getX() + 1, getY() ) == UnitType.player )
						adjacentPlayers.add(Direction.right);
				
				// Pick a random player to attack
				if (adjacentPlayers.size() == 0)
					continue; // There are no players to attack
				Direction playerToAttack = adjacentPlayers.get( (int)(Math.random() * adjacentPlayers.size()) );
				
				// Attack the player
				switch (playerToAttack) {
					case up:
						this.dealDamage( getX(), getY() - 1, this.getAttackPoints() );
						break;
					case right:
						this.dealDamage( getX() + 1, getY(), this.getAttackPoints() );
						break;
					case down:
						this.dealDamage( getX(), getY() + 1, this.getAttackPoints() );
						break;
					case left:
						this.dealDamage( getX() - 1, getY(), this.getAttackPoints() );
						break;
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			InfluxLogger.getInstance().logUnitRoundDuration(this, this.node.getServerAddress(), System.currentTimeMillis() - start);
		}
		//node.unRegister();

	}

}
