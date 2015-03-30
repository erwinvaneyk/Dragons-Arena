package distributed.systems.das.units.impl;

import java.util.ArrayList;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.BattleField;
import distributed.systems.das.units.Dragon;
import distributed.systems.network.ClientNode;

public class StationaryDragon extends Dragon {

	public StationaryDragon(int x, int y, ClientNode node) throws AlreadyAssignedIDException {
		super(x, y, node);
	}

	protected void doAction() {
		ArrayList<Direction> adjacentPlayers = new ArrayList<>();
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
			return; // There are no players to attack
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
	}
}
