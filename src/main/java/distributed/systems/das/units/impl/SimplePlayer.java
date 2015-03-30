package distributed.systems.das.units.impl;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import distributed.systems.core.LogMessage;
import distributed.systems.core.LogType;
import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.units.Dragon;
import distributed.systems.das.units.Player;
import distributed.systems.das.units.Unit;
import distributed.systems.network.ClientNode;

/**
 * the simple players’ in-game actions are derived from a simple strategy:
 * - heal a nearby (adjacent in this case) player as soon as there is one that has below 50% of its initial hp
 * - go towards the closest dragon and strike otherwise
 */
public class SimplePlayer extends RandomPlayer {

	public SimplePlayer(int maxHealth, int attackPoints, ClientNode node) throws AlreadyAssignedIDException {
		super(maxHealth, attackPoints, node);
	}

	@Override
	protected void doAction() {
		// Get nearby hurt players
		List<Unit> adjacentUnit = getAdjacentUnits();
		List<Unit> adjacentHurtPlayers = adjacentUnit.stream()
				.filter(unit -> unit.getType() == UnitType.player
						&& unit.getHitPoints() <= (unit.getMaxHitPoints() / 2))
				.collect(toList());

		// Heal a random hurt player if applicable
		if(adjacentHurtPlayers.size() > 0) {
			Unit playerToHeal = adjacentHurtPlayers.get((int) (Math.random() * adjacentHurtPlayers.size()));
			healDamage(playerToHeal.getX(), playerToHeal.getY(), getAttackPoints());
			return;
		}

		// find nearest dragon
		Optional<Dragon> targetDragon = getNearest(UnitType.dragon).map(unit -> (Dragon) unit);

		// Go to and fight nearest dragon
		if(targetDragon.isPresent()) {
			Dragon dragon = targetDragon.get();
			node.sendMessageToServer(new LogMessage("Targeting dragon at position (" + dragon.getX() + "," + dragon.getY() + ")",
					LogType.DEBUG));
			if (this.distanceTo(dragon.getX(), dragon.getY()) == 1) {
				// Stab dragon if in range
				dealDamage(dragon.getX(), dragon.getY(), getAttackPoints());
			}
			else {
				// Move to dragon using (not so) advanced path-finding
				moveTowards(dragon.getX(), getY(), adjacentUnit);
			}
		} else {
			// No dragon around? Move mindlessly around :)
			node.sendMessageToServer(new LogMessage("No dragon found. Going for a mindless walk.", LogType.DEBUG));
			this.doRandomMove();
		}
	}


	// Very dumb path-finding, try to move in a straight line to the target
	private boolean moveTowards(int x, int y, List<Unit> adjacentUnits) {
		int newX = (int) Math.signum(this.x - x);
		int newY = (int) Math.signum(this.y - y);
		// Straight line
		if ((newX == 0 || newY == 0) && locationIsFree(this.x + newX, this.y + newY, adjacentUnits)) {
			this.moveUnit(this.x + newX,this.y + newY);
		} else {
			// Diagonal line
			if(locationIsFree(this.x, this.y + newY, adjacentUnits)) {
				this.moveUnit(this.x + newX,this.y + newY);
			} else if(locationIsFree(this.x + newX, this.y, adjacentUnits)) {
				this.moveUnit(this.x + newX,this.y + newY);
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean locationIsFree(int x, int y, List<Unit> adjacentUnits) {
		return adjacentUnits.stream()
				.noneMatch(unit -> unit.getX() == x && unit.getY() == y);
	}
}
