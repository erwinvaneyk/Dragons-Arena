package distributed.systems.das.units.impl;

import java.util.ArrayList;
import java.util.List;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.BattleField;
import distributed.systems.das.units.Dragon;
import distributed.systems.das.units.Unit;
import distributed.systems.network.ClientNode;

public class StationaryDragon extends Dragon {

	public StationaryDragon(int x, int y, ClientNode node) throws AlreadyAssignedIDException {
		super(x, y, node);
	}

	protected void doAction() {
		// Pick a random player to attack
		List<Unit> adjacentPlayers = getAdjacentPlayers();

		if (adjacentPlayers.size() > 0) {
			Unit playerToAttack = adjacentPlayers.get( (int)(Math.random() * adjacentPlayers.size()) );
			// Attack the player
			playerToAttack.dealDamage(playerToAttack.getX(), playerToAttack.getY(), this.getAttackPoints());
		}
	}

	// TODO: move to function in battlefield
	private List<Unit> getAdjacentPlayers() {
		List<Unit> adjacentPlayers = new ArrayList<>();
		// Decide what players are near
		if (getY() > 0) {
			Unit unit = getUnit(getX(), getY() - 1);
			if (unit.getType() == UnitType.player)
				adjacentPlayers.add(unit);
		}
		if (getY() < BattleField.MAP_WIDTH - 1) {
			Unit unit = getUnit(getX(), getY() + 1);
			if (unit.getType() == UnitType.player)
				adjacentPlayers.add(unit);
		}
		if (getX() > 0) {
			Unit unit = getUnit(getX() - 1, getY());
			if (unit.getType() == UnitType.player)
				adjacentPlayers.add(unit);
		}
		if (getX() < BattleField.MAP_WIDTH - 1) {
			Unit unit = getUnit(getX() + 1, getY() - 1);
			if (unit.getType() == UnitType.player)
				adjacentPlayers.add(unit);
		}
		return adjacentPlayers;
	}
}
