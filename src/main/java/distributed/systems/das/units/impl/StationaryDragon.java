package distributed.systems.das.units.impl;

import static java.util.stream.Collectors.toList;

import java.util.List;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.units.Dragon;
import distributed.systems.das.units.Unit;
import distributed.systems.network.ClientNode;

public class StationaryDragon extends Dragon {

	public StationaryDragon(int x, int y, ClientNode node) throws AlreadyAssignedIDException {
		super(x, y, node);
	}

	protected void doAction() {
		// Pick a random player to attack
		List<Unit> adjacentPlayers = this.getAdjacentUnits().stream()
				.filter(unit -> unit.getType() == UnitType.player)
				.collect(toList());

		if (adjacentPlayers.size() > 0) {
			Unit playerToAttack = adjacentPlayers.get( (int)(Math.random() * adjacentPlayers.size()) );
			// Attack the player
			dealDamage(playerToAttack.getX(), playerToAttack.getY(), this.getAttackPoints());
		}
	}
}
