package distributed.systems.das.units.impl;

import distributed.systems.core.exception.AlreadyAssignedIDException;
import distributed.systems.das.BattleField;
import distributed.systems.das.units.Player;
import distributed.systems.network.ClientNode;

public class RandomPlayer extends Player {

	public RandomPlayer(int x, int y, ClientNode node) throws AlreadyAssignedIDException {
		super(x, y, node);
	}

	protected void doAction() {
		Direction direction;
		UnitType adjacentUnitType;
		int targetX = 0, targetY = 0;

		// Randomly choose one of the four wind directions to move to if there are no units present
		direction = Direction.values()[ (int)(Direction.values().length * Math.random()) ];

		switch (direction) {
			case up:
				if (this.getY() <= 0)
					// The player was at the edge of the map, so he can't move north and there are no units there
					return;

				targetX = this.getX();
				targetY = this.getY() - 1;
				break;
			case down:
				if (this.getY() >= BattleField.MAP_HEIGHT - 1)
					// The player was at the edge of the map, so he can't move south and there are no units there
					return;

				targetX = this.getX();
				targetY = this.getY() + 1;
				break;
			case left:
				if (this.getX() <= 0)
					// The player was at the edge of the map, so he can't move west and there are no units there
					return;

				targetX = this.getX() - 1;
				targetY = this.getY();
				break;
			case right:
				if (this.getX() >= BattleField.MAP_WIDTH - 1)
					// The player was at the edge of the map, so he can't move east and there are no units there
					return;

				targetX = this.getX() + 1;
				targetY = this.getY();
				break;
		}

		// Get what unit lies in the target square
		adjacentUnitType = this.getType(targetX, targetY);

		switch (adjacentUnitType) {
			case undefined:
				// There is no unit in the square. Move the player to this square
				this.moveUnit(targetX, targetY);
				break;
			case player:
				// There is a player in the square, attempt a healing
				this.healDamage(targetX, targetY, getAttackPoints());
				break;
			case dragon:
				// There is a dragon in the square, attempt a dragon slaying
				this.dealDamage(targetX, targetY, getAttackPoints());
				break;
		}
	}
}
