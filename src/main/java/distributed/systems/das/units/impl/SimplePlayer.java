package distributed.systems.das.units.impl;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.units.Player;
import distributed.systems.network.ClientNode;

/**
 * the simple players’ in-game actions are derived from a simple strategy:
 * - heal a nearby (adjacent in this case) player as soon as there is one that has below 50% of its initial hp
 * - go towards the closest dragon and strike otherwise
 */
public class SimplePlayer extends Player {

	public SimplePlayer(int maxHealth, int attackPoints, ClientNode node) throws AlreadyAssignedIDException {
		super(maxHealth, attackPoints, node);
	}

	@Override
	protected void doAction() {

	}
}
